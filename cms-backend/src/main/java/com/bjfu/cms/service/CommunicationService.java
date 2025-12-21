package com.bjfu.cms.service;

import com.bjfu.cms.entity.InternalMessage;
import java.util.List;

public interface CommunicationService {
    void sendMessage(Integer senderId, Integer receiverId, String topic, String title, String content);

    List<InternalMessage> getMyMessages();

    // 【新增】根据 Topic (稿件ID) 获取当前用户的相关消息历史
    List<InternalMessage> getMessagesByTopic(String topic);

    void markRead(Integer messageId);
}