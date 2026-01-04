package com.bjfu.cms.service;

import com.bjfu.cms.entity.InternalMessage;
import com.bjfu.cms.entity.dto.ChatSessionDTO;

import java.util.List;

public interface CommunicationService {
    void sendMessage(Integer senderId, Integer receiverId, String topic, String title, String content, Integer msgType);

    // 获取当前用户的系统通知 (Type=0)
    List<InternalMessage> getSystemNotifications();

    // 获取指定稿件的聊天记录 (Type=1, Topic="MS-{id}")
    List<InternalMessage> getChatHistory(String topic);

//    List<InternalMessage> getMyMessages();
//
//    // 【新增】根据 Topic (稿件ID) 获取当前用户的相关消息历史
//    List<InternalMessage> getMessagesByTopic(String topic);

    void markRead(Integer messageId);

    List<InternalMessage> getMyAllMessages();

    void markAllSystemAsRead(Integer userId);
    // 在 CommunicationService 接口中添加：
    List<ChatSessionDTO> getChatSessions();
}