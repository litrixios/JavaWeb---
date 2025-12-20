package com.bjfu.cms.entity;
import lombok.Data;
import java.util.Date;

@Data
public class InternalMessage {
    private Integer messageId;
    private Integer senderId;
    private Integer receiverId;
    private String topic;
    private String title;
    private String content;
    private Boolean isRead;
    private Date sendTime;

    // === 【新增】用于展示发件人信息的字段 ===
    // 这些字段在 InternalMessage 表里没有，是靠 User 表联查出来的
    private String senderName; // 发件人姓名 (FullName 或 Username)
    private String senderRole; // 发件人角色 (用于判断是否显示“回复”按钮)
}