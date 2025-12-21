package com.bjfu.cms.service.impl;

import com.bjfu.cms.common.result.Result;
import com.bjfu.cms.common.utils.UserContext;
import com.bjfu.cms.entity.News;
import com.bjfu.cms.entity.File;
import com.bjfu.cms.mapper.NewsMapper;
import com.bjfu.cms.service.NewsService;
import com.bjfu.cms.service.SftpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;

@Service
public class NewsServiceImpl implements NewsService {

    @Autowired
    private NewsMapper newsMapper;
    @Autowired
    private SftpService sftpService;  // 注入SFTP服务

    @Value("${aliyun.ecs.remote-dir}")
    private String remoteBaseDir;  // 远程根目录
    @Value("${file.temp.path}")
    private String localTempPath;  // 本地临时目录


    @Override
    public List<News> getAllNews(String keyword, String startDate, String endDate) {
        return newsMapper.selectAllNews(keyword, startDate, endDate);
    }

    @Override
    public News getNewsById(Integer newsId) {
        return newsMapper.selectNewsById(newsId);
    }

    @Override
    @Transactional
    public Result<Integer> addNews(News news) {
        Integer currentUserId = UserContext.getUserId();
        news.setPublisherId(currentUserId);

        // 关键调整：根据是否有定时时间，设置初始状态
        if (news.getScheduledPublishDate() != null) {
            // 定时发布：publishDate存定时时间，初始不显示（IsActive=false）
            news.setPublishDate(news.getScheduledPublishDate());
            news.setIsActive(false); // 未到时间不显示
        } else {
            // 立即发布：publishDate存当前时间，立即显示（IsActive=true）
            news.setPublishDate(new Date());
            news.setIsActive(true); // 立即显示
        }

        // 注意：scheduledPublishDate不会存入数据库（表中无此字段），仅用它临时传递定时时间
        newsMapper.insertNews(news);
        return Result.success(news.getNewsId());
    }

    @Override
    @Transactional
    public Result<String> updateNews(News news) {
        // 若更新时传递了定时时间，同步更新publishDate和IsActive
        if (news.getScheduledPublishDate() != null) {
            news.setPublishDate(news.getScheduledPublishDate());
            news.setIsActive(false); // 重新定时后暂不显示
        }
        newsMapper.updateNews(news);
        return Result.success("新闻更新成功");
    }

    @Service
    @EnableScheduling // 开启定时任务支持（若全局已配置可省略）
    public class NewsPublishSchedule {

        @Autowired
        private NewsMapper newsMapper;

        // 每1分钟执行一次（可根据需求调整频率）
        @Scheduled(cron = "0 0/1 * * * ?")
        public void checkAndPublish() {
            Date now = new Date();
            // 查询：到了发布时间（publishDate <= 现在）且未激活（IsActive=false）的新闻
            List<News> pendingNews = newsMapper.selectPendingPublishNews(now);

            if (!pendingNews.isEmpty()) {
                // 批量更新为激活状态（IsActive=true）
                List<Integer> newsIds = pendingNews.stream()
                        .map(News::getNewsId)
                        .collect(Collectors.toList());
                newsMapper.batchUpdateIsActive(newsIds, true);
                System.out.println("定时发布完成，共" + newsIds.size() + "条新闻");
            }
        }
    }


    @Override
    @Transactional
    public Result<String> deleteNews(Integer newsId) {
        try {
            // 1. 查询该新闻关联的所有附件
            List<File> fileList = newsMapper.selectFilesByNewsId(newsId);

            // 2. 遍历删除远程文件
            for (File file : fileList) {
                sftpService.deleteRemoteFile(file.getFilePath());  // 远程删除
                newsMapper.deleteNewsFile(file.getFileId());  // 数据库删除
            }

            // 3. 删除新闻记录
            newsMapper.deleteNews(newsId);

            return Result.success("新闻及关联附件删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("新闻删除失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<String> uploadNewsFile(Integer newsId, MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }

        try {
            // 1. 准备目录和文件名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) {
                return Result.error("文件格式不合法，无扩展名");
            }
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension; // 唯一文件名

            // 2. 远程存储路径（按新闻ID分目录，避免文件冲突）
            String remoteDir = remoteBaseDir + "/news/" + newsId;  // 如：/root/cms/files/news/1001
            String remoteFilePath = remoteDir + "/" + uniqueFileName;

            // 3. 通过SFTP上传（直接使用MultipartFile的输入流）
            sftpService.upload(file.getInputStream(), uniqueFileName, remoteDir);

            // 4. 数据库记录远程文件路径（完整远程路径，方便后续下载/删除）
            File fileEntity = new File();
            fileEntity.setFileName(originalFilename);
            fileEntity.setFilePath(remoteFilePath);  // 存储远程完整路径
            fileEntity.setUploadTime(new Date());
            fileEntity.setUploaderId(UserContext.getUserId());
            fileEntity.setNewsId(newsId);  // 关联新闻
            newsMapper.insertNewsFile(fileEntity, newsId);

            return Result.success("文件上传成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }

    @Override
    public List<File> getFilesByNewsId(Integer newsId) {
        return newsMapper.selectFilesByNewsId(newsId);
    }

    @Override
    @Transactional
    public Result<String> deleteNewsFile(Integer fileId) {
        try {
            // 1. 查询文件信息（获取远程路径）
            File file = newsMapper.selectFileById(fileId);
            if (file == null) {
                return Result.error("文件记录不存在");
            }

            // 2. 通过SFTP删除远程文件
            sftpService.deleteRemoteFile(file.getFilePath());  // 直接使用数据库存储的远程路径

            // 3. 删除数据库记录
            newsMapper.deleteNewsFile(fileId);

            return Result.success("文件删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("文件删除失败：" + e.getMessage());
        }
    }
    // 在NewsServiceImpl中实现下载方法
    @Override
    public Result<Resource> downloadNewsFile(Integer fileId) {
        try {
            // 1. 查询文件信息获取远程路径
            com.bjfu.cms.entity.File file = newsMapper.selectFileById(fileId); // 明确使用自定义File实体
            if (file == null) {
                return Result.error("文件不存在");
            }
            String remoteFilePath = file.getFilePath();
            if (remoteFilePath == null || remoteFilePath.isEmpty()) {
                return Result.error("文件路径不存在");
            }

            // 2. 确保本地临时目录存在（使用java.io.File）
            java.io.File localDir = new java.io.File(localTempPath); // 这里现在使用的是java.io.File，带参数构造方法有效
            if (!localDir.exists()) {
                localDir.mkdirs();
            }

            // 3. 调用SFTP服务下载文件到本地临时目录
            sftpService.download(remoteFilePath, localTempPath);

            // 4. 构建本地文件资源
            String fileName = remoteFilePath.substring(remoteFilePath.lastIndexOf("/") + 1);
            java.io.File localFile = new java.io.File(localTempPath + java.io.File.separator + fileName);
            if (!localFile.exists()) {
                return Result.error("文件下载失败，本地文件未找到");
            }

            // 5. 返回资源对象
            FileSystemResource resource = new FileSystemResource(localFile);
            return Result.success(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("文件下载失败：" + e.getMessage());
        }
    }
}