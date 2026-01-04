package com.bjfu.cms.service.strategy;

import com.bjfu.cms.entity.InternalMessage;
import com.bjfu.cms.entity.User;
import com.bjfu.cms.mapper.InternalMessageMapper;
import com.bjfu.cms.mapper.UserMapper;
import com.bjfu.cms.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

public abstract class AbstractMessageStrategy {

    @Autowired
    protected InternalMessageMapper messageMapper;
    @Autowired
    protected UserMapper userMapper;
    @Autowired
    protected EmailService emailService;

    /**
     * 【模板方法】统一的发送流程
     */
    public void executeSend(Integer senderId, Integer receiverId, String topic, String title, String content, Integer msgType) {
        // 1. 准备数据
        User sender = userMapper.selectById(senderId);
        User receiver = userMapper.selectById(receiverId);

        if (receiver == null) {
            throw new RuntimeException("接收用户不存在");
        }

        // 2. 权限校验，这里是由子类来实现的
        checkPermission(sender, receiver, msgType, topic);

        // 3. 存入数据库 (站内信)
        InternalMessage msg = new InternalMessage();
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setTopic(topic);
        msg.setTitle(title);
        msg.setContent(content);
        msg.setMsgType(msgType);
        msg.setSendTime(new Date());
        msg.setIsRead(false);

        messageMapper.insertMessage(msg);

        // 4. 异步发送邮件通知
        sendEmailNotification(receiver, topic, title, content);
    }

    /**
     * 抽象方法：子类必须实现具体的权限规则
     */
    protected abstract void checkPermission(User sender, User receiver, Integer msgType, String topic);

    // 辅助方法：发邮件
    private void sendEmailNotification(User receiver, String topic, String title, String content) {
        if (receiver.getEmail() != null && !receiver.getEmail().isEmpty()) {
            try {
                String subject = "【" + topic + "】" + title;
                String body = content + "<br><hr><small>系统自动通知，请勿回复</small>";
                // null 是抄送，暂时先填null
                emailService.sendHtmlMail(receiver.getEmail(), null, subject, body);
            } catch (Exception e) {
                System.err.println("邮件发送失败，但不影响站内信保存: " + e.getMessage());
            }
        }
    }
}