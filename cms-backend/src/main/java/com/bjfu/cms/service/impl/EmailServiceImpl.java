package com.bjfu.cms.service.impl;

import com.bjfu.cms.mapper.SystemConfigMapper;
import com.bjfu.cms.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private SystemConfigMapper configMapper;

    private JavaMailSenderImpl getMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        // 动态读取配置
        String host = configMapper.getValue("mail_host");
        String portStr = configMapper.getValue("mail_port");
        String username = configMapper.getValue("mail_username");
        String password = configMapper.getValue("mail_password");

        // 简单的非空检查，防止空指针
        if (host == null || portStr == null || username == null || password == null) {
            System.err.println("邮件配置缺失，请检查数据库 SystemConfig 表");
            return sender;
        }

        int port = Integer.parseInt(portStr);

        sender.setHost(host);
        sender.setPort(port);
        sender.setUsername(username);
        sender.setPassword(password);
        sender.setDefaultEncoding("UTF-8");

        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.timeout", "5000"); // 设置超时时间，防止卡死
        props.put("mail.smtp.connectiontimeout", "5000");

        // === 关键修复：根据端口判断加密方式 ===
        if (port == 465) {
            // 465 端口通常使用 SSL
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        } else {
            // 587 或其他端口通常使用 STARTTLS
            props.put("mail.smtp.starttls.enable", "true");
        }

        return sender;
    }

    @Override
    @Async // 注意：需要在启动类加 @EnableAsync 才会生效
    public void sendHtmlMail(String to, String[] cc, String subject, String htmlContent) {
        try {
            System.out.println("正在尝试发送邮件给: " + to + "...");
            JavaMailSenderImpl sender = getMailSender();

            // 检查配置是否加载成功
            if (sender.getHost() == null) {
                System.err.println("发送失败：邮件配置未加载。");
                return;
            }

            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(sender.getUsername());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true 表示支持 HTML

            if (cc != null && cc.length > 0) {
                helper.setCc(cc);
            }

            sender.send(message);
            System.out.println("✅ 邮件成功发送至: " + to);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ 邮件发送失败: " + e.getMessage());
        }
    }
}