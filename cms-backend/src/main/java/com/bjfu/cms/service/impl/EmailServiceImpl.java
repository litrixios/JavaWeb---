package com.bjfu.cms.service.impl;

import com.bjfu.cms.mapper.SystemConfigMapper;
import com.bjfu.cms.service.EmailService;
import jakarta.mail.MessagingException;
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
        sender.setHost(configMapper.getValue("mail_host"));
        String port = configMapper.getValue("mail_port");
        sender.setPort(port != null ? Integer.parseInt(port) : 587);
        sender.setUsername(configMapper.getValue("mail_username"));
        sender.setPassword(configMapper.getValue("mail_password"));
        sender.setDefaultEncoding("UTF-8");

        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        return sender;
    }

    @Override
    @Async
    public void sendHtmlMail(String to, String[] cc, String subject, String htmlContent) {
        try {
            JavaMailSenderImpl sender = getMailSender();
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(sender.getUsername());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            if (cc != null && cc.length > 0) {
                helper.setCc(cc);
            }

            sender.send(message);
            System.out.println("邮件已发送至: " + to);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("邮件发送失败: " + e.getMessage());
        }
    }
}