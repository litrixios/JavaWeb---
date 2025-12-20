package com.bjfu.cms.service.impl;

import com.bjfu.cms.common.result.Result;
import com.bjfu.cms.common.utils.UserContext;
import com.bjfu.cms.entity.News;
import com.bjfu.cms.entity.File;
import com.bjfu.cms.mapper.NewsMapper;
import com.bjfu.cms.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class NewsServiceImpl implements NewsService {

    @Autowired
    private NewsMapper newsMapper;

    @Value("${file.upload.path}")
    private String uploadPath;

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

        // 如果设置了定时发布时间，则使用该时间，否则使用当前时间
        if (news.getScheduledPublishDate() != null) {
            news.setPublishDate(news.getScheduledPublishDate());
        } else {
            news.setPublishDate(new Date());
        }

        news.setIsActive(true);
        newsMapper.insertNews(news);
        return Result.success(news.getNewsId());
    }

    @Override
    @Transactional
    public Result<String> updateNews(News news) {
        newsMapper.updateNews(news);
        return Result.success("新闻更新成功");
    }

    @Override
    @Transactional // 事务保证：附件删除和新闻删除要么都成功，要么都回滚
    public Result<String> deleteNews(Integer newsId) {
        try {
            // ========== 第一步：查询该新闻关联的所有附件 ==========
            List<File> fileList = newsMapper.selectFilesByNewsId(newsId);

            // ========== 第二步：遍历附件，删除存储的文件 + 删除附件数据库记录 ==========
            for (File file : fileList) {
                // ========== 核心配套修改：拼接完整路径 ==========
                String uniqueFileName = file.getFilePath(); // 数据库中存的是唯一文件名
                Path absFilePath = Paths.get(uploadPath).resolve(uniqueFileName); // 拼接路径
                java.io.File localFile = absFilePath.toFile();

                // ========== 新增：删除本地文件 ==========
                if (localFile.exists()) {
                    localFile.delete();
                }

                // 2. 删除Files表中的附件记录（可复用原有方法）
                newsMapper.deleteNewsFile(file.getFileId());
            }

            // ========== 第三步：删除新闻记录（此时无外键约束冲突） ==========
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
            // 1. 确保上传目录存在（原有逻辑不变）
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 2. 生成唯一文件名（原有逻辑不变）
            String originalFilename = file.getOriginalFilename();
            // 处理文件名可能为null的情况（健壮性优化）
            if (originalFilename == null || !originalFilename.contains(".")) {
                return Result.error("文件格式不合法，无扩展名");
            }
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            // 3. 保存文件（原有逻辑不变）
            Path filePath = uploadDir.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath);

            // 4. 记录文件信息到数据库（核心修改：关联newsId）
            File fileEntity = new File();
            fileEntity.setFileName(originalFilename);
            fileEntity.setFilePath(uniqueFileName);
            fileEntity.setUploadTime(new Date());
            fileEntity.setUploaderId(UserContext.getUserId());
            // 关键：调用Mapper时传入newsId，将文件与新闻关联
            newsMapper.insertNewsFile(fileEntity, newsId);

            return Result.success("文件上传成功");
        } catch (IOException e) {
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
            // 第一步：先从数据库查询文件信息（获取filePath）
            File file = newsMapper.selectFileById(fileId); // 需补充该查询方法
            if (file == null) {
                return Result.error("文件记录不存在");
            }

            String uniqueFileName = file.getFilePath(); // 数据库中现在存的是唯一文件名
            Path absFilePath = Paths.get(uploadPath).resolve(uniqueFileName); // 拼接完整路径
            java.io.File localFile = absFilePath.toFile();

            if (localFile.exists()) {
                boolean isDeleted = localFile.delete();
                if (!isDeleted) {
                    System.err.println("本地文件删除失败：" + absFilePath);
                }
            }

            // 第三步：删除数据库记录
            newsMapper.deleteNewsFile(fileId);

            return Result.success("文件删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("文件删除失败：" + e.getMessage());
        }
    }
}