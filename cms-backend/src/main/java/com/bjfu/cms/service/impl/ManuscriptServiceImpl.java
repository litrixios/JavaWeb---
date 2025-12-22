package com.bjfu.cms.service.impl;

import com.bjfu.cms.entity.User; // 引入User
import com.bjfu.cms.common.utils.UserContext;
import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.ManuscriptMeta;
import com.bjfu.cms.entity.SystemLog;
import com.bjfu.cms.entity.dto.ManuscriptDTO;
import com.bjfu.cms.entity.dto.ManuscriptTrackDTO;
import com.bjfu.cms.mapper.ManuscriptMapper;
import com.bjfu.cms.mapper.ManuscriptMetaMapper;
import com.bjfu.cms.mapper.UserMapper;
import com.bjfu.cms.service.CommunicationService;
import com.bjfu.cms.service.EmailService;
import com.bjfu.cms.service.ManuscriptService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class ManuscriptServiceImpl implements ManuscriptService {

    @Autowired
    private ManuscriptMapper manuscriptMapper;

    @Autowired
    private ManuscriptMetaMapper manuscriptMetaMapper;

    @Autowired
    private UserMapper userMapper; // 新增：用于查询用户信息(邮箱等)

    @Autowired
    private EmailService emailService; // 新增：邮件服务

    @Autowired
    private CommunicationService communicationService; // 新增：站内信服务

    @Autowired
    private ObjectMapper objectMapper;

    // 假设系统管理员/系统通知账号ID为 1
    private static final Integer SYSTEM_ADMIN_ID = 1;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitManuscript(ManuscriptDTO dto) {
        Integer userId = UserContext.getUserId();

        // 1. 组装主表数据 (Manuscript)
        Manuscript manuscript = new Manuscript();
        manuscript.setManuscriptId(dto.getManuscriptId());
        manuscript.setAuthorId(userId);
        manuscript.setTitle(dto.getTitle());
        manuscript.setAbstractText(dto.getAbstractText());
        manuscript.setKeywords(dto.getKeywords());
        manuscript.setFundingInfo(dto.getFundingInfo());
        manuscript.setSubmissionTime(new Date());

        // 设置状态
        boolean isOfficialSubmit = "SUBMIT".equalsIgnoreCase(dto.getActionType());
        if (isOfficialSubmit) {
            manuscript.setStatus("Processing");
            manuscript.setSubStatus("TechCheck");
        } else {
            manuscript.setStatus("Incomplete");
            manuscript.setSubStatus(null);
        }

        try {
            if (dto.getAuthors() != null) {
                manuscript.setAuthorList(objectMapper.writeValueAsString(dto.getAuthors()));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("作者信息格式化失败", e);
        }

        // 2. 保存/更新主表
        if (manuscript.getManuscriptId() == null) {
            manuscriptMapper.insertManuscript(manuscript);
            dto.setManuscriptId(manuscript.getManuscriptId());
        } else {
            manuscriptMapper.updateManuscript(manuscript);
        }

        // 3. 组装扩展表 (ManuscriptMeta)
        ManuscriptMeta meta = new ManuscriptMeta();
        meta.setManuscriptId(dto.getManuscriptId());
        meta.setTopic(dto.getTopic());
        meta.setCoverLetterContent(dto.getCoverLetterContent());

        try {
            if (dto.getRecommendedReviewers() != null) {
                meta.setRecommendedReviewers(objectMapper.writeValueAsString(dto.getRecommendedReviewers()));
            }
        } catch (JsonProcessingException e) {
            meta.setRecommendedReviewers("[]");
        }

        if (manuscriptMetaMapper.selectByManuscriptId(dto.getManuscriptId()) == null) {
            manuscriptMetaMapper.insert(meta);
        } else {
            manuscriptMetaMapper.updateByManuscriptId(meta);
        }

        // 4. 版本记录
        if (dto.getOriginalFilePath() != null && !dto.getOriginalFilePath().isEmpty()) {
            int versionNumber = 1;
            manuscriptMapper.insertVersion(
                    manuscript.getManuscriptId(),
                    versionNumber,
                    dto.getOriginalFilePath(),
                    dto.getAnonymousFilePath(),
                    dto.getCoverLetterPath(),
                    null,
                    null
            );
        }

        // === 5. 新增：通知逻辑 (仅在正式提交时触发) ===
        if (isOfficialSubmit) {
            sendSubmissionNotifications(userId, manuscript);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitRevision(ManuscriptDTO dto) {
        // 1. 校验
        Manuscript manuscript = manuscriptMapper.selectById(dto.getManuscriptId());
        if (manuscript == null) {
            throw new RuntimeException("稿件不存在");
        }
        // 根据业务需求，通常只允许在 'Revision' 或特定状态下修回
        // if (!"Revision".equalsIgnoreCase(manuscript.getStatus())) { ... }

        // 2. 插入新版本
        Integer maxVersion = manuscriptMapper.selectMaxVersion(dto.getManuscriptId());
        int nextVersion = (maxVersion == null ? 0 : maxVersion) + 1;

        manuscriptMapper.insertVersion(
                dto.getManuscriptId(),
                nextVersion,
                dto.getOriginalFilePath(),
                dto.getAnonymousFilePath(),
                dto.getCoverLetterPath(),
                dto.getMarkedFilePath(),
                dto.getResponseLetterPath()
        );

        // 3. 更新状态
        manuscript.setStatus("Processing");
        manuscript.setSubStatus("TechCheck"); // 或 "WithEditor"，视流程而定
        manuscript.setSubmissionTime(new Date());
        manuscript.setDecision(null); // 清空旧决策
        manuscript.setDecisionTime(null);

        manuscriptMapper.updateManuscript(manuscript);

        // === 4. 新增：通知逻辑 (修回通知) ===
        sendRevisionNotifications(manuscript);
    }

    @Override
    public PageInfo<Manuscript> getManuscriptList(Integer pageNum, Integer pageSize, String status, String subStatus) {
        Integer userId = UserContext.getUserId();
        if (status != null && status.trim().isEmpty()) status = null;
        if (subStatus != null && subStatus.trim().isEmpty()) subStatus = null;
        PageHelper.startPage(pageNum, pageSize);
        List<Manuscript> list = manuscriptMapper.selectManuscripts(userId, status, subStatus);
        return new PageInfo<>(list);
    }

    // ================== 私有辅助方法：发送通知 ==================

    /**
     * 处理新投稿的通知：发邮件给作者 + 站内信通知作者
     */
    private void sendSubmissionNotifications(Integer authorId, Manuscript manuscript) {
        // 1. 获取作者信息
        User author = userMapper.selectById(authorId);
        if (author == null) return;

        String title = manuscript.getTitle();
        String manuscriptIdStr = "MS-" + manuscript.getManuscriptId(); // 模拟稿件编号

        // 2. 发送邮件确认
        String emailSubject = "【投稿确认】您的稿件已成功提交: " + manuscriptIdStr;
        String emailContent = "<h3>尊敬的 " + author.getUsername() + ":</h3>" +
                "<p>您好！感谢您向本期刊投稿。</p>" +
                "<p>您的稿件 <b>" + title + "</b> 已成功接收，系统编号为 <b>" + manuscriptIdStr + "</b>。</p>" +
                "<p>您可以在系统中追踪稿件的最新状态。</p>" +
                "<br/><p>此邮件由系统自动发送，请勿回复。</p>";

        // 异步发送邮件
        emailService.sendHtmlMail(author.getEmail(), null, emailSubject, emailContent);

        // TODO 等待系统管理员站内信功能
//        // 3. 发送站内信 (系统 -> 作者)
//        // 假设 SYSTEM_ADMIN_ID 为发件人
//        String msgTitle = "投稿成功通知";
//        String msgContent = "您的稿件《" + title + "》已成功提交，目前正在进行形式审查。";
//
//        // 调用站内信服务
//        communicationService.sendMessage(SYSTEM_ADMIN_ID, authorId, "Submission", msgTitle, msgContent);
    }

    /**
     * 处理修回稿件的通知：发邮件给作者 + 站内信通知负责编辑
     */
    private void sendRevisionNotifications(Manuscript manuscript) {
        // 1. 获取作者信息
        User author = userMapper.selectById(manuscript.getAuthorId());
        if (author == null) return;

        String title = manuscript.getTitle();

        // 2. 发送邮件确认给作者
        String emailSubject = "【修回确认】您的稿件修改版已提交";
        String emailContent = "<h3>尊敬的 " + author.getUsername() + ":</h3>" +
                "<p>您好！</p>" +
                "<p>您的稿件 <b>" + title + "</b> 的修改版本（Revision）已成功上传。</p>" +
                "<p>编辑部将尽快处理您的稿件。</p>";

        emailService.sendHtmlMail(author.getEmail(), null, emailSubject, emailContent);

        // 3. 发送站内信通知编辑 (如果当前有负责编辑)
        if (manuscript.getCurrentEditorId() != null) {
            String msgTitle = "收到修回稿件";
            String msgContent = "作者 " + author.getUsername() + " 已提交稿件《" + title + "》的修改版本，请及时处理。";

            // 作者 -> 编辑 (或者 系统 -> 编辑)
            communicationService.sendMessage(
                    SYSTEM_ADMIN_ID, // 发件人
                    manuscript.getCurrentEditorId(), // 收件人: 当前编辑
                    "Revision",
                    msgTitle,
                    msgContent
            );
        }
    }

    @Override
    public ManuscriptTrackDTO trackManuscript(Integer manuscriptId) {
        // 1. 获取当前登录用户，做权限校验（只能看自己的稿件）
        Integer currentUserId = UserContext.getUserId();

        Manuscript manuscript = manuscriptMapper.selectById(manuscriptId);
        if (manuscript == null) {
            throw new RuntimeException("稿件不存在");
        }

        // 简单校验：作者只能看自己的，或者你有更复杂的权限系统
        if (!manuscript.getAuthorId().equals(currentUserId)) {
            // 可以在这里判断是否是管理员/主编，如果是则放行，否则抛异常
            // throw new RuntimeException("无权查看此稿件的追踪信息");
        }

        // 2. 查询日志历史
        List<SystemLog> logs = manuscriptMapper.selectLogsByManuscriptId(manuscriptId);

        // 3. 组装 DTO
        ManuscriptTrackDTO trackDTO = new ManuscriptTrackDTO();
        trackDTO.setManuscript(manuscript);
        trackDTO.setHistoryLogs(logs);

        // 4. 设置预计周期 (简单硬编码，或者根据 Status 动态判断)
        if ("Under Review".equals(manuscript.getStatus())) {
            trackDTO.setEstimatedCycle("预计 4-6 周");
        } else {
            trackDTO.setEstimatedCycle("处理中");
        }

        return trackDTO;
    }
}