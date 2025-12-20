package com.bjfu.cms.service;

import com.bjfu.cms.entity.InternalMessage;
import java.util.List;

public interface CommunicationService {
    void sendMessage(Integer senderId, Integer receiverId, String topic, String title, String content);
    List<InternalMessage> getMyMessages();
    void markRead(Integer messageId);
}