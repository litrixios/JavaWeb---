package com.bjfu.cms.entity.dto;

import lombok.Data;

@Data
public class MessageSendDTO {
    private Integer receiverId;
    private String topic;
    private String title;
    private String content;
    /**
     * 【新增】消息类型
     * 0: 系统通知 (单向，由系统触发或管理员群发)
     * 1: 聊天沟通 (双向，作者<->编辑，基于稿件)
     */
    private Integer msgType;

}
