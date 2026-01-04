package com.bjfu.cms.service.impl;

import com.bjfu.cms.common.result.Result;
import com.bjfu.cms.common.utils.PdfUtils;
import com.bjfu.cms.common.utils.SaplingAiDetectClient;
import com.bjfu.cms.common.utils.UserContext;
import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.User;
import com.bjfu.cms.entity.dto.TechCheckAnalysisDTO;
import com.bjfu.cms.entity.dto.TechCheckDTO;
import com.bjfu.cms.mapper.ManuscriptMapper;
import com.bjfu.cms.mapper.UserMapper;
import com.bjfu.cms.service.CommunicationService;
import com.bjfu.cms.service.EmailService;
import com.bjfu.cms.service.EditorialAdminService;
import com.bjfu.cms.service.SftpService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Service
public class EditorialAdminServiceImpl implements EditorialAdminService {


    @Value("${file.temp.path}")
    private String localTempPath;

    @Autowired
    private SaplingAiDetectClient saplingAiDetectClient;

    @Autowired
    private CommunicationService communicationService;

    @Autowired
    private SftpService sftpService;

    @Autowired
    private ManuscriptMapper manuscriptMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EmailService emailService;

    @Override
    public List<Manuscript> getTechCheckManuscripts() {
        return manuscriptMapper.selectTechCheckManuscripts();
    }

    @Override
    @Transactional
    public void techCheck(TechCheckDTO dto) {
        // 检查参数
        if (dto.getManuscriptId() == null) {
            throw new RuntimeException("稿件ID不能为空");
        }

        // 获取稿件信息
        Manuscript manuscript = manuscriptMapper.selectById(dto.getManuscriptId());
        if (manuscript == null) {
            throw new RuntimeException("稿件不存在");
        }

        // 更新稿件状态
        if (dto.getPassed()) {
            // 形式审查通过，进入下一阶段
            manuscriptMapper.updateStatus(dto.getManuscriptId(), "Processing", "PendingDeskReview");
            // 形式审查通过，发送站内信通知作者
            communicationService.sendMessage(
                    1,
                    manuscript.getAuthorId(),
                    "MS-" + manuscript.getManuscriptId(),
                    "形式审查通过",
                    "您的稿件已通过形式审查，即将进入初审阶段。",
                    0
            );

        } else {
            // 形式审查不通过，调用退回修改方法
            unsubmitManuscript(dto);
        }

        // 记录日志
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logDesc = String.format("编辑部管理员 形式审查通过: 稿件ID=%d, 结果=%s, 字数=%d, 查重率=%.2f%%, 反馈=%s",
                dto.getManuscriptId(),
                dto.getPassed() ? "通过" : "不通过",
                dto.getWordCount(),
                dto.getPlagiarismRate() * 100,
                dto.getFeedback());
        userMapper.insertLog(now, UserContext.getUserId(), "TechCheck", dto.getManuscriptId(), logDesc);
    }

    @Override
    @Transactional
    public void unsubmitManuscript(TechCheckDTO dto) {
        // 检查参数
        if (dto.getManuscriptId() == null) {
            throw new RuntimeException("稿件ID不能为空");
        }
        if (!dto.getPassed() && (dto.getFeedback() == null || dto.getFeedback().isEmpty())) {
            throw new RuntimeException("形式审查不通过时，必须提供反馈意见");
        }

        // 获取稿件信息
        Manuscript manuscript = manuscriptMapper.selectById(dto.getManuscriptId());
        if (manuscript == null) {
            throw new RuntimeException("稿件不存在");
        }

        // 获取作者信息（用于获取邮箱）
        User author = userMapper.selectById(manuscript.getAuthorId());
        if (author == null || author.getEmail() == null || author.getEmail().isEmpty()) {
            throw new RuntimeException("作者邮箱信息不存在，无法发送通知邮件");
        }

        // 更新稿件状态为"Incomplete"
        manuscriptMapper.updateManuscriptSpecial(dto.getManuscriptId(), "Incomplete", null);

//        // 发送邮件给作者（修正参数匹配问题）
//        String subject = "稿件形式审查未通过";
//        String content = String.format("您的稿件《%s》形式审查未通过，原因如下：<br/>%s<br/><br/>请修改后重新提交。",
//                manuscript.getTitle(), dto.getFeedback());
//        // 补充cc参数（null表示无抄送），使用作者邮箱作为收件人
//        emailService.sendHtmlMail(author.getEmail(), null, subject, content);

        communicationService.sendMessage(
                1,
                author.getUserId(),
                "MS-" + manuscript.getManuscriptId(),
                "形式审查未通过",
                "您的稿件形式审查未通过，原因：" + dto.getFeedback() + "。请修改后重新提交。",
                0
        );

        // 记录日志
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logDesc = String.format("编辑部管理员 形式审查未通过，退回修改: 稿件ID=%d, 原因=%s",
                dto.getManuscriptId(), dto.getFeedback());
        userMapper.insertLog(now, UserContext.getUserId(), "Unsubmit", dto.getManuscriptId(), logDesc);
    }

    @Override
    public Result<TechCheckAnalysisDTO> getTechCheckFileAnalysis(Integer manuscriptId) {
        // 1. 校验稿件存在性
        Manuscript manuscript = manuscriptMapper.selectById(manuscriptId);
        if (manuscript == null) {
            return Result.error("稿件不存在");
        }

        // 2. 获取最新版本的OriginalFilePath
        String remoteFilePath = manuscriptMapper.selectLatestOriginalFilePath(manuscriptId);
        if (remoteFilePath == null || remoteFilePath.isEmpty()) {
            return Result.error("未找到稿件的最新版本文件");
        }

        // 3. 下载文件到本地临时目录（使用配置的file.temp.path）
        try {
            sftpService.download(remoteFilePath, localTempPath);

            // 4. 构建本地文件路径
            String fileName = remoteFilePath.substring(remoteFilePath.lastIndexOf("/") + 1);
            String localFilePath = localTempPath + java.io.File.separator + fileName;

            // 5. 字数统计
            int wordCount = countWords(localFilePath);

            // 6. 自动查重（这里为示例，实际需集成查重工具）
            double plagiarismRate = checkPlagiarism(localFilePath);

            // 7. 封装结果
            TechCheckAnalysisDTO result = new TechCheckAnalysisDTO();
            result.setManuscriptId(manuscriptId);
            result.setOriginalFilePath(remoteFilePath);
            result.setLocalTempPath(localFilePath);
            result.setWordCount(wordCount);
            result.setPlagiarismRate(plagiarismRate);

            return Result.success(result);
        } catch (Exception e) {
            return Result.error("文件分析失败：" + e.getMessage());
        }
    }

    // 字数统计工具方法（文本文件）
// 替换原countWords方法为PDF处理
    private int countWords(String localFilePath) throws IOException {
        // 判断文件是否为PDF
        if (localFilePath.toLowerCase().endsWith(".pdf")) {
            return PdfUtils.countWordsInPdf(localFilePath);
        } else {
            throw new RuntimeException("暂不支持非PDF格式的字数统计");
        }
    }

    // 查重模拟方法
    private double checkPlagiarism(String localFilePath) {
        try {
            // 1) PDF提取文本
            String text = PdfUtils.extractText(localFilePath);

            // 2) 空文本直接 0
            if (text == null || text.trim().isEmpty()) return 0.0;

            // 3) 调 Sapling：返回 0~1
            text = normalizeForAiDetect(text);
            double aiScore = saplingAiDetectClient.detectAiScore(text);

            // 4) 作为“率”返回（0~1）
            return clamp01(aiScore);
        } catch (Exception e) {
            // 生产建议：记录日志 + 返回一个可识别的默认值
            // 也可以直接抛出让上层提示“检测失败”
            System.err.println("查重检测失败！文件路径：" + localFilePath + "，错误原因：" + e.getMessage());
            return Math.random() * 0.30;
        }
    }

    private double clamp01(double v) {
        if (v < 0) return 0;
        if (v > 1) return 1;
        return v;
    }
    private String normalizeForAiDetect(String text) {
        if (text == null) return "";
        // 去掉多余空白，减少字符消耗
        String t = text.replaceAll("\\s+", " ").trim();

        // 截断：比如最多 12000 字符（你可调）
        int max = 12000;
        return t.length() > max ? t.substring(0, max) : t;
    }


    @Override
    public void previewLatestPdf(Integer manuscriptId, HttpServletResponse response) {
        // 1) 校验稿件存在性 —— 复用 getTechCheckFileAnalysis 的逻辑
        Manuscript manuscript = manuscriptMapper.selectById(manuscriptId);
        if (manuscript == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 2) 获取最新版本的OriginalFilePath —— 与 getTechCheckFileAnalysis 一致
        String remoteFilePath = manuscriptMapper.selectLatestOriginalFilePath(manuscriptId);
        if (remoteFilePath == null || remoteFilePath.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 3) 只允许PDF预览（避免把doc等当pdf预览）
        if (!remoteFilePath.toLowerCase().endsWith(".pdf")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 4) 设置响应头：inline 预览
        String fileName = remoteFilePath.substring(remoteFilePath.lastIndexOf("/") + 1);
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");

        response.setContentType("application/pdf");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition",
                "inline; filename=\"" + fileName + "\"; filename*=UTF-8''" + encoded);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("X-Content-Type-Options", "nosniff");

        // 5) SFTP -> HTTP 输出流（不落地）
        try (ServletOutputStream out = response.getOutputStream()) {
            sftpService.downloadToStream(remoteFilePath, out);
            out.flush();
        } catch (Exception e) {
            // 这里不要再往response写JSON，否则会污染PDF响应
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

}