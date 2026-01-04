package com.bjfu.cms.service.impl;

import com.bjfu.cms.common.utils.UserContext;
import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.User;
import com.bjfu.cms.entity.dto.ReviewTrackingDTO;
import com.bjfu.cms.mapper.EditorMapper;
import com.bjfu.cms.mapper.ManuscriptMapper; // 确保有这个Mapper
import com.bjfu.cms.mapper.UserMapper;
import com.bjfu.cms.service.EditorService;
import com.bjfu.cms.service.EmailService;
import jakarta.servlet.ServletOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
// 确保原有注入的 SftpService 和 ManuscriptMapper 正常

@Service
public class EditorServiceImpl implements EditorService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EditorMapper editorMapper;

    // 修复点 1: 必须注入 ManuscriptMapper 才能调用 selectById
    @Autowired
    private ManuscriptMapper manuscriptMapper;
    // 注入通信服务，用于发送站内信
    @Autowired
    private com.bjfu.cms.service.CommunicationService communicationService;

    @Autowired
    private EmailService emailService;

    @Value("${file.temp.path}")
    private String localTempPath;

    @Autowired
    private com.bjfu.cms.service.SftpService sftpService;




    @Override
    public void sendRemindMail(Integer manuscriptId, Integer reviewId, String content) {
        // 1. 获取催审详情 (SQL 中可以增加对 manuscriptId 的匹配校验)
        Map<String, Object> detail = editorMapper.selectReviewDetailForRemind(reviewId);

        if (detail == null) {
            throw new RuntimeException("催审失败：未找到相关评审任务。");
        }

        // 校验：确保该评审任务确实属于传入的稿件 ID
        Integer actualMsId = (Integer) detail.get("manuscript_id");
        if (actualMsId != null && !actualMsId.equals(manuscriptId)) {
            throw new RuntimeException("催审失败：稿件与评审任务不匹配。");
        }

        String subject = "【催审通知】稿件(ID:" + manuscriptId + ")评审进度提醒：" + detail.get("manuscriptTitle");

        communicationService.sendMessage(
                1,
                reviewId,
                "MS-" + manuscriptId,
                subject,
                content,
                0
        );
    }

    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨 2 点执行一次
    public void autoCheckOverdueReviews() {
        // 1. 查找逾期正好 7 天且状态为 'Invited' 或 'Accepted' (未完成) 的评审
        // 这里的逻辑交给 Mapper 去筛选日期
        List<Map<String, Object>> overdueReviews = editorMapper.selectOverdueSevenDays();

        for (Map<String, Object> review : overdueReviews) {
            Integer msId = (Integer) review.get("ManuscriptID");
            Integer rId = (Integer) review.get("ReviewID");
            String title = (String) review.get("Title");

            String autoContent = "尊敬的审稿人，您负责的稿件《" + title + "》已逾期 7 天，请尽快提交审稿意见。";

            try {
                // 直接复用你写好的催审方法
                this.sendRemindMail(msId, rId, autoContent);
                System.out.println("自动催审已发送：稿件ID " + msId);
            } catch (Exception e) {
                // 防止单个失败影响整体扫描
                e.printStackTrace();
            }
        }
    }

    @Override
    @Transactional
    public void submitToEIC(Manuscript m) {
        // 1. 获取完整的稿件信息
        Manuscript originalMs = manuscriptMapper.selectById(m.getManuscriptId());
        if (originalMs == null) throw new RuntimeException("稿件不存在");

        Integer currentUserId = UserContext.getUserId();
        m.setCurrentEditorId(currentUserId);

        // 2. 更新数据库中的建议和报告
        editorMapper.updateRecommendation(m);

        // 3. 获取主编 ID
        Integer eicId = editorMapper.findUserByRole("EditorInChief");

        // 4. 获取当前编辑的姓名（增加健壮性检查）
        User user = userMapper.selectById(currentUserId);
        // 如果查不到 User 对象，则降级显示“未知编辑”或直接显示 ID
        String editorName = (user != null) ? user.getFullName() : "ID:" + currentUserId;

        if (eicId != null) {
            communicationService.sendMessage(
                    1, // 发送者 ID (系统)
                    eicId,
                    "MS-" + m.getManuscriptId(),
                    "稿件建议提醒: " + originalMs.getTitle(),
                    "编辑 [" + editorName + "] 已提交建议结论：" + m.getEditorRecommendation() + "。请及时审核。",
                    0
            );
        }
    }

    @Override
    public List<Manuscript> getAssignedList() {
        // 修复点 2: 补充缺失的接口实现，从当前上下文获取编辑ID
        Integer editorId = UserContext.getUserId();
        return editorMapper.selectMyManuscripts(editorId);
    }

    @Override
    public List<User> getAllReviewers() {
        return editorMapper.selectUsersByRole("reviewer");
    }

    @Override
    @Transactional
    public void inviteReviewers(Integer msId, List<Integer> rIds) {
        Date now = new Date();
        Date deadline = new Date(System.currentTimeMillis() + 14L * 24 * 60 * 60 * 1000);

        // 1. 插入审稿记录
        editorMapper.batchInsertReviews(msId, rIds, now, deadline);

        // 2. 更新状态 (必须严格匹配数据库的 CHECK 约束值)
        // 根据你的约束：Status 应为 'Processing', SubStatus 应为 'UnderReview'
        editorMapper.updateManuscriptStatus(msId, "Processing", "UnderReview");
    }


    @Override
    public Manuscript getById(Integer id) {
        // 修复点 3: 确保 manuscriptMapper 已注入且有 selectById 方法
        // 如果没有通用 Mapper，则需要在 EditorMapper 里写 SQL
        return manuscriptMapper.selectById(id);
    }

    @Override
    public void downloadManuscript(Integer manuscriptId, HttpServletResponse response) throws Exception {
        // 日志 1：进入方法
        System.out.println(">>>> 开始处理下载请求，稿件ID: " + manuscriptId);

        String remoteFilePath = manuscriptMapper.selectLatestOriginalFilePath(manuscriptId);
        System.out.println(">>>> 数据库查到的远程路径: " + remoteFilePath);

        if (remoteFilePath == null || remoteFilePath.isEmpty()) throw new RuntimeException("文件路径为空");

        File tempDir = new File(localTempPath);
        if (!tempDir.exists()) {
            boolean created = tempDir.mkdirs(); // mkdirs 会连同父目录一起创建
            System.out.println(">>>> 临时目录不存在，创建结果: " + created);
        }

        // 2. SFTP 下载
        sftpService.download(remoteFilePath, localTempPath);

        String fileName = remoteFilePath.substring(remoteFilePath.lastIndexOf("/") + 1);
        File localFile = new File(localTempPath + File.separator + fileName);

        // 日志 2：检查本地文件状态
        if (!localFile.exists()) {
            System.err.println(">>>> 错误：本地临时文件不存在! 路径: " + localFile.getAbsolutePath());
            throw new IOException("文件下载到本地失败");
        }
        System.out.println(">>>> 本地文件已生成，大小: " + localFile.length() + " bytes");

        if (localFile.length() == 0) {
            System.err.println(">>>> 警告：本地文件大小为 0，下载可能已损坏");
        }

        // 3. 准备响应
        // 关键：暂时注释掉 response.reset()，看看是否是它冲突了
        // response.reset();

        response.setContentType("application/pdf");
        response.setHeader("Content-Length", String.valueOf(localFile.length()));

        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + encodedFileName + ";filename*=utf-8''" + encodedFileName);
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition, Content-Length");

        // 4. 流拷贝（手动控制以确保不报 Broken Pipe）
        try (FileInputStream fis = new FileInputStream(localFile);
             BufferedInputStream bis = new BufferedInputStream(fis);
             OutputStream os = response.getOutputStream()) {

            System.out.println(">>>> 开始向响应流写入数据...");
            byte[] buffer = new byte[8192];
            int len;
            int totalWrite = 0;
            while ((len = bis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
                totalWrite += len;
            }
            os.flush();
            System.out.println(">>>> 数据写入完成，总计发送: " + totalWrite + " bytes");
        } catch (Exception e) {
            System.err.println(">>>> 写入过程中发生异常: " + e.getMessage());
        } finally {
            // 5. 延迟删除：确保流关闭后再删
            if (localFile.exists()) {
                boolean deleted = localFile.delete();
                System.out.println(">>>> 临时文件删除结果: " + deleted);
            }
        }
    }

    @Override
    public List<User> recommendReviewers(Integer msId) {
        // 1. 获取当前稿件详情（使用你 Mapper 中的 selectById）
        Manuscript ms = manuscriptMapper.selectById(msId); //
        if (ms == null) return new ArrayList<>();

        // 2. 获取作者信息以进行机构避嫌
        User author = userMapper.selectById(ms.getAuthorId()); // 假设 UserMapper 也有 selectById
        String authorAffiliation = (author != null) ? author.getAffiliation() : "";

        // 3. 处理稿件关键词（分词处理）
        String[] msKeywords = ms.getKeywords() != null
                ? ms.getKeywords().split("[;；,，\\s+]")
                : new String[0];

        // 4. 获取所有待选审稿人（角色为 Reviewer）
        // 这里会拿到你之前 SQL 算出来的 activeTasks
        List<User> allReviewers = editorMapper.selectUsersByRole("Reviewer");

        // 5. 算法打分逻辑
        for (User reviewer : allReviewers) {
            double score = 0.0;

            // 【机构避嫌】同单位直接排除
            if (authorAffiliation != null && !authorAffiliation.isEmpty()
                    && authorAffiliation.equals(reviewer.getAffiliation())) {
                reviewer.setRecommendScore(-999.0);
                continue;
            }

            // 【专业匹配】关键词重合度 (Manuscript.keywords vs User.researchDirection)
            if (reviewer.getResearchDirection() != null && msKeywords.length > 0) {
                for (String kw : msKeywords) {
                    String cleanKw = kw.trim();
                    if (!cleanKw.isEmpty() && reviewer.getResearchDirection().contains(cleanKw)) {
                        score += 10.0; // 命中一个词加 10 分
                    }
                }
            }

            // 【负载平衡】在审稿件数越少，分越高 (使用你加的 activeTasks 字段)
            int tasks = (reviewer.getActiveTasks() != null) ? reviewer.getActiveTasks() : 0; //
            score -= (tasks * 2.0); // 每多 1 篇扣 2 分

            reviewer.setRecommendScore(score);
        }

        // 6. 排序并过滤
        return allReviewers.stream()
                .filter(r -> r.getRecommendScore() > -100) // 过滤掉被排除的同单位人员
                .sorted((r1, r2) -> r2.getRecommendScore().compareTo(r1.getRecommendScore()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewTrackingDTO> getReviewTrackingList(Integer editorId) {
        // 调用我们刚刚在 XML 里写的聚合查询
        return manuscriptMapper.selectTrackingWithOpinions(editorId);
    }
} // 所有的实现方法必须在这个大括号内！