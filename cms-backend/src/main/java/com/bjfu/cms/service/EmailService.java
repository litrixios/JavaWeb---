package com.bjfu.cms.service;

public interface EmailService {
    /**
     * 发送 HTML 格式邮件
     * @param to 收件人邮箱
     * @param cc 抄送人邮箱 (可为 null)
     * @param subject 邮件主题
     * @param htmlContent 邮件正文 (支持 HTML)
     */
    void sendHtmlMail(String to, String[] cc, String subject, String htmlContent);
}