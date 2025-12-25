package com.bjfu.cms.entity.dto;

import java.util.Date;
import lombok.Data;

@Data
public class ChatSessionDTO {
    private String topic;          // 会话主题，例如 "MS-10"
    private Integer manuscriptId;  // 稿件ID
    private String manuscriptTitle;// 稿件标题

    // 对方的信息 (接收人)
    private Integer targetUserId;
    private String targetUserName;
    private String targetRole; // 对方角色

    // 会话预览信息
    private String lastMessageContent;
    private Date lastMessageTime;
    private Integer unreadCount; // 未读消息数

}