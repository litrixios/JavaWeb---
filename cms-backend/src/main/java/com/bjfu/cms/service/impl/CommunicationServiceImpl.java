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
    public void sendMessage(Integer senderId, Integer receiverId, String topic, String title, String content) {
        // 1. 获取发送者信息
        User sender = userMapper.selectById(senderId);
        if (sender == null) {
            throw new RuntimeException("发送者不存在");
        }

        // 2. 拼接策略名称 (数据库Role字段 + "_STRATEGY")
        // 例如：Role是 "Author"，拼接成 "Author_STRATEGY"
        String strategyName = sender.getRole() + "_STRATEGY";

        // 3. 从 Map 中取出对应的策略处理类
        AbstractMessageStrategy strategy = strategyMap.get(strategyName);

        if (strategy == null) {
            // 如果没找到特定策略（比如管理员可能没写策略），可以抛错或者用默认策略
            // 这里为了调试方便，直接打印错误
            System.err.println("未找到角色策略: " + strategyName);
            throw new RuntimeException("该角色暂无发送消息权限或策略未定义");
        }

        // 4. 执行策略 (模板方法)
        strategy.executeSend(senderId, receiverId, topic, title, content);
    }

    @Override
    public List<InternalMessage> getMyMessages() {
        Integer currentUserId = UserContext.getUserId();
        return messageMapper.selectByReceiverId(currentUserId);
    }

    @Override
    public void markRead(Integer messageId) {
        messageMapper.markAsRead(messageId);
    }

    @Override
    public List<InternalMessage> getMessagesByTopic(String topic) {
        Integer currentUserId = UserContext.getUserId();
        // 调用 Mapper 新增的方法，查询 topic 匹配且与当前用户有关（发件人或收件人）的消息
        return messageMapper.selectByTopicAndUser(topic, currentUserId);
    }
}