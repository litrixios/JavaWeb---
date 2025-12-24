package com.bjfu.cms.service.impl;

import com.bjfu.cms.common.utils.UserContext;
import com.bjfu.cms.entity.InternalMessage;
import com.bjfu.cms.entity.User;
import com.bjfu.cms.mapper.InternalMessageMapper;
import com.bjfu.cms.mapper.UserMapper;
import com.bjfu.cms.service.CommunicationService;
import com.bjfu.cms.service.strategy.AbstractMessageStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CommunicationServiceImpl implements CommunicationService {

    @Autowired
    private InternalMessageMapper messageMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 【核心】策略地图
     * Spring 会自动扫描所有继承 AbstractMessageStrategy 的 @Component
     * Key = Bean的名字 (例如 "Author_STRATEGY")
     * Value = 对应的策略对象
     */
    @Autowired
    private Map<String, AbstractMessageStrategy> strategyMap;

    @Override
    public void sendMessage(Integer senderId, Integer receiverId, String topic, String title, String content, Integer msgType) {
        User sender = userMapper.selectById(senderId);
        String strategyName = sender.getRole() + "_STRATEGY";
        AbstractMessageStrategy strategy = strategyMap.get(strategyName);

        if (strategy == null) {
            // 系统自动发送的消息可能没有sender(senderId=0或-1)，需要特殊处理，或者赋予管理员策略
            throw new RuntimeException("未找到发送策略");
        }

        strategy.executeSend(senderId, receiverId, topic, title, content, msgType);
    }

    @Override
    public List<InternalMessage> getSystemNotifications() {
        Integer userId = UserContext.getUserId();
        // 需要在 XML 实现 selectByTypeAndReceiver(type=0, userId)
        return messageMapper.selectByTypeAndReceiver(0, userId);
    }

    @Override
    public List<InternalMessage> getChatHistory(String topic) {
        Integer userId = UserContext.getUserId();
        // 获取该稿件下的聊天 (Type=1)
        // XML selectByTopicAndUser 需要增加 AND msg_type = 1
        return messageMapper.selectByTopicAndUser(topic, userId);
    }

    @Override
    public void markRead(Integer messageId) {
        messageMapper.markAsRead(messageId);
    }

}