package com.bjfu.cms.service.impl;

import com.bjfu.cms.common.utils.UserContext;
import com.bjfu.cms.entity.InternalMessage;
import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.User;
import com.bjfu.cms.entity.dto.ChatSessionDTO;
import com.bjfu.cms.mapper.InternalMessageMapper;
import com.bjfu.cms.mapper.ManuscriptMapper;
import com.bjfu.cms.mapper.UserMapper;
import com.bjfu.cms.service.CommunicationService;
import com.bjfu.cms.service.strategy.AbstractMessageStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommunicationServiceImpl implements CommunicationService {

    @Autowired
    private InternalMessageMapper messageMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ManuscriptMapper manuscriptMapper;

    @Autowired
    private Map<String, AbstractMessageStrategy> strategyMap;

    @Override
    public void sendMessage(Integer senderId, Integer receiverId, String topic, String title, String content, Integer msgType) {
        User sender = userMapper.selectById(senderId);
        String strategyName = sender.getRole() + "_STRATEGY";
        AbstractMessageStrategy strategy = strategyMap.get(strategyName);

        if (strategy == null) {
            throw new RuntimeException("未找到发送策略");
        }
        strategy.executeSend(senderId, receiverId, topic, title, content, msgType);
    }

    @Override
    public List<InternalMessage> getMyAllMessages() {
        Integer userId = UserContext.getUserId();
        return messageMapper.selectAllMessagesForUser(userId);
    }

    @Override
    public List<InternalMessage> getSystemNotifications() {
        Integer userId = UserContext.getUserId();
        return messageMapper.selectByTypeAndReceiver(0, userId);
    }

    @Override
    public List<InternalMessage> getChatHistory(String topic) {
        Integer userId = UserContext.getUserId();
        return messageMapper.selectByTopicAndUser(topic, userId);
    }

    @Override
    public void markRead(Integer messageId) {
        messageMapper.markAsRead(messageId);
    }

    @Override
    public List<ChatSessionDTO> getChatSessions() {
        Integer userId = UserContext.getUserId();
        User currentUser = userMapper.selectById(userId);
        List<ChatSessionDTO> sessions = new ArrayList<>();

        // 1. 获取该用户相关的所有稿件
        List<Manuscript> manuscripts;
        if ("AUTHOR".equalsIgnoreCase(currentUser.getRole())) {
            manuscripts = manuscriptMapper.selectByAuthorId(userId);
        } else if ("EDITOR".equalsIgnoreCase(currentUser.getRole())) {
            manuscripts = manuscriptMapper.selectByEditorId(userId);
        } else {
            manuscripts = new ArrayList<>();
        }

        // 2. 获取该用户的所有消息并按 Topic 分组
        List<InternalMessage> allMessages = messageMapper.selectAllMessagesForUser(userId);
        Map<String, List<InternalMessage>> msgGroup = allMessages.stream()
                .filter(m -> m.getMsgType() == 1) // 仅筛选聊天类型的消息
                .collect(Collectors.groupingBy(InternalMessage::getTopic));

        // 3. 遍历稿件构建 Session
        for (Manuscript m : manuscripts) {
            // ========================= 核心修复开始 =========================

            // 针对作者角色的特殊过滤
            if ("AUTHOR".equalsIgnoreCase(currentUser.getRole())) {
                // 1. 业务逻辑限制：必须已分配编辑才能开始聊天
                //    如果 CurrentEditorID 为 null，说明还在 TechCheck 或 PendingAssign 阶段
                if (m.getCurrentEditorId() == null) {
                    continue;
                }

                // 2. 数据安全防御：防止出现非本人的稿件 (理论上Mapper层已过滤，这里做双重保险)
                if (m.getAuthorId() != null && !m.getAuthorId().equals(userId)) {
                    continue;
                }
            }

            // ========================= 核心修复结束 =========================

            ChatSessionDTO dto = new ChatSessionDTO();
            String topic = "MS-" + m.getManuscriptId();

            dto.setTopic(topic);
            dto.setManuscriptId(m.getManuscriptId());
            dto.setManuscriptTitle(m.getTitle());

            // 确定聊天对象
            if ("AUTHOR".equalsIgnoreCase(currentUser.getRole())) {
                dto.setTargetUserId(m.getCurrentEditorId());
                dto.setTargetUserName("编辑");
                dto.setTargetRole("Editor");
            } else {
                // 如果是编辑，聊天对象是作者
                dto.setTargetUserId(m.getAuthorId());
                dto.setTargetUserName("作者");
                dto.setTargetRole("Author");
            }

            // 填充最新消息和未读数
            if (msgGroup.containsKey(topic)) {
                List<InternalMessage> msgs = msgGroup.get(topic);
                if (!msgs.isEmpty()) {
                    InternalMessage latest = msgs.get(0); // 假设SQL已经按时间倒序排列
                    dto.setLastMessageContent(latest.getContent());
                    dto.setLastMessageTime(latest.getSendTime());

                    long unread = msgs.stream()
                            .filter(msg -> msg.getReceiverId().equals(userId) && Boolean.FALSE.equals(msg.getIsRead()))
                            .count();
                    dto.setUnreadCount((int) unread);
                }
            } else {
                // 没有历史消息时显示默认提示
                dto.setLastMessageContent("暂无消息");
                dto.setUnreadCount(0);
            }
            sessions.add(dto);
        }

        return sessions;
    }

    @Override
    public void markAllSystemAsRead(Integer userId) {
        messageMapper.markSystemMessagesAsRead(userId);
    }
}