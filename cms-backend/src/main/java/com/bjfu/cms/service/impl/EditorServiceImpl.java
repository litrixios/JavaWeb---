package com.bjfu.cms.service.impl;

import com.bjfu.cms.common.utils.UserContext;
import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.User;
import com.bjfu.cms.mapper.EditorMapper;
import com.bjfu.cms.mapper.ManuscriptMapper; // 确保有这个Mapper
import com.bjfu.cms.service.EditorService;
import com.bjfu.cms.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.FileCopyUtils; // Spring自带的流拷贝工具，非常方便
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
// 确保原有注入的 SftpService 和 ManuscriptMapper 正常

@Service
public class EditorServiceImpl implements EditorService {

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
    public void sendRemindMail(Integer reviewId, String content) {
        // 1. 获取催审详情
        Map<String, Object> detail = editorMapper.selectReviewDetailForRemind(reviewId);

        if (detail == null || detail.get("email") == null) {
            throw new RuntimeException("催审失败：未找到审稿人邮箱信息。");
        }

        String toEmail = (String) detail.get("email");
        String manuscriptTitle = (String) detail.get("title");

        // 2. 构建邮件主题
        String subject = "【催审通知】稿件评审进度提醒：" + manuscriptTitle;

        // 3. 调用你已有的异步发送方法
        // 注意：content 已经在前端通过模板生成，包含了 HTML 标签
        emailService.sendHtmlMail(toEmail, null, subject, content);

        System.out.println("催审任务已提交至发送队列，Target: " + toEmail);
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

        if (eicId != null) {
            communicationService.sendMessage(
                    UserContext.getUserId(),
                    eicId,
                    "MS-" + m.getManuscriptId(),
                    "稿件建议提醒: " + originalMs.getTitle(),
                    "编辑已提交建议结论：" + m.getEditorRecommendation() + "。请及时审核。"
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
} // 所有的实现方法必须在这个大括号内！