package com.bjfu.cms.service.impl;

import com.bjfu.cms.common.utils.UserContext;
import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.User;
import com.bjfu.cms.mapper.EditorMapper;
import com.bjfu.cms.mapper.ManuscriptMapper; // 确保有这个Mapper
import com.bjfu.cms.mapper.UserMapper;
import com.bjfu.cms.service.EditorService;
import com.bjfu.cms.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

    @Override
    @Transactional
    public void submitToEIC(Manuscript m) {
        // 1. 获取完整的稿件信息（为了拿到标题和主编信息）
        Manuscript originalMs = manuscriptMapper.selectById(m.getManuscriptId());
        m.setCurrentEditorId(UserContext.getUserId());

        // 2. 更新数据库中的建议和报告
        editorMapper.updateRecommendation(m);

        // 3. 自动发送消息给主编 (假设系统中角色为 'EditorInChief' 的用户是接收者)
        // 注意：实际项目中可能需要根据配置获取主编ID，这里示例获取 ID 为 1 的主编或通过 Role 查询
        Integer eicId = editorMapper.findUserByRole("EditorInChief");

        User user = userMapper.selectById(UserContext.getUserId());

        if (eicId != null) {
            communicationService.sendMessage(
                    1,
                    eicId,
                    "MS-" + m.getManuscriptId(),
                    "稿件建议提醒: " + originalMs.getTitle(),
                    "编辑" + user.getFullName() + "已提交建议结论：" + m.getEditorRecommendation() + "。请及时审核。",
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
        // 1. 获取路径并下载
        String remoteFilePath = manuscriptMapper.selectLatestOriginalFilePath(manuscriptId);
        if (remoteFilePath == null || remoteFilePath.isEmpty()) throw new RuntimeException("文件不存在");

        sftpService.download(remoteFilePath, localTempPath);

        String fileName = remoteFilePath.substring(remoteFilePath.lastIndexOf("/") + 1);
        File localFile = new File(localTempPath + File.separator + fileName);

        if (!localFile.exists()) throw new IOException("文件下载失败");

        // 2. 【关键】清除之前的状态，确保 Header 生效
        response.reset();

        // 3. 【关键】设置 Header 必须在获取 OutputStream 之前
        response.setContentType("application/octet-stream");
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

        // 设置下载头
        response.setHeader("Content-Disposition", "attachment;filename=" + encodedFileName + ";filename*=utf-8''" + encodedFileName);
        // 暴露 Header 让前端 axios 能读取
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

        // 4. 写入流
        try (FileInputStream fis = new FileInputStream(localFile)) {
            org.springframework.util.FileCopyUtils.copy(fis, response.getOutputStream());
            response.getOutputStream().flush();
        } finally {
            if (localFile.exists()) localFile.delete(); // 清理临时文件
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

} // 所有的实现方法必须在这个大括号内！