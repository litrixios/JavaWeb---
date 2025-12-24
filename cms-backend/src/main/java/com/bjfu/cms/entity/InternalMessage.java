// 修改文件: cms-backend/src/main/java/com/bjfu/cms/entity/InternalMessage.java
package com.bjfu.cms.entity;
import lombok.Data;
import java.util.Date;

@Data
public class InternalMessage {
    private Integer messageId;
    private Integer senderId;
    private Integer receiverId;

    // 约定：如果是聊天消息，Topic必须为 "MS-{manuscriptId}"
    // 如果是系统消息，Topic可以是 "SYSTEM" 或 "MS-{manuscriptId}" (关联稿件的系统通知)
    private String topic;

    private String title;
    private String content;
    private Boolean isRead;
    private Date sendTime;

    /**
     * 【新增】消息类型
     * 0: 系统通知 (单向，由系统触发或管理员群发)
     * 1: 聊天沟通 (双向，作者<->编辑，基于稿件)
     */
    private Integer msgType;

    // === 联查字段 ===
    private String senderName;
    private String senderRole;
}