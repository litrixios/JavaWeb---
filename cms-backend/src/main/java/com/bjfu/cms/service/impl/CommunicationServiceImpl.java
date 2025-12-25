package com.bjfu.cms.service.impl;

import com.bjfu.cms.common.utils.UserContext;
import com.bjfu.cms.entity.InternalMessage;
import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.User;
import com.bjfu.cms.entity.dto.ChatSessionDTO;
import com.bjfu.cms.mapper.InternalMessageMapper;
import com.bjfu.cms.mapper.ManuscriptMapper;
import com.bjfu.cms.mapper.ManuscriptMetaMapper;
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
    public List<InternalMessage> getMyAllMessages() {
        Integer userId = UserContext.getUserId();
        return messageMapper.selectAllMessagesForUser(userId);
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

    @Override
    public List<ChatSessionDTO> getChatSessions() {
        Integer userId = UserContext.getUserId();
        User currentUser = userMapper.selectById(userId);
        List<ChatSessionDTO> sessions = new ArrayList<>();

        // 1. 获取该用户相关的所有稿件
        List<Manuscript> manuscripts;
        if ("AUTHOR".equalsIgnoreCase(currentUser.getRole())) {
            // 调用刚在 Mapper 中新增的方法
            manuscripts = manuscriptMapper.selectByAuthorId(userId);
        } else if ("EDITOR".equalsIgnoreCase(currentUser.getRole())) {
            // 调用刚在 Mapper 中新增的方法
            manuscripts = manuscriptMapper.selectByEditorId(userId);
        } else {
            manuscripts = new ArrayList<>();
        }

        // 2. 获取该用户的所有消息
        List<InternalMessage> allMessages = messageMapper.selectAllMessagesForUser(userId);
        Map<String, List<InternalMessage>> msgGroup = allMessages.stream()
                .filter(m -> m.getMsgType() == 1)
                .collect(Collectors.groupingBy(InternalMessage::getTopic));

        // 3. 遍历构建 Session
        for (Manuscript m : manuscripts) {
            ChatSessionDTO dto = new ChatSessionDTO();
            String topic = "MS-" + m.getManuscriptId();

            dto.setTopic(topic);
            dto.setManuscriptId(m.getManuscriptId());
            dto.setManuscriptTitle(m.getTitle());

            // 确定聊天对象
            if ("AUTHOR".equalsIgnoreCase(currentUser.getRole())) {
                dto.setTargetUserId(m.getCurrentEditorId());
                dto.setTargetUserName(m.getCurrentEditorId() == null ? "待分配编辑" : "编辑"); // 简单处理名字，实际可查表
                dto.setTargetRole("Editor");
            } else {
                // 如果我是编辑，对象就是作者
                // 注意：这里 AuthorName 可能在 selectByEditorId 中未联查出来，
                // 如果 Manuscript 实体里没有 authorName 字段值，可能需要额外处理。
                // 不过 selectByEditorId 返回的是 Manuscript 对象，通常含 AuthorID。
                // 暂时用 "作者" 代替名字，或者前端再查一次。
                dto.setTargetUserId(m.getAuthorId());
                dto.setTargetUserName("作者");
                dto.setTargetRole("Author");
            }

            // 填充消息数据
            if (msgGroup.containsKey(topic)) {
                List<InternalMessage> msgs = msgGroup.get(topic);
                if (!msgs.isEmpty()) {
                    InternalMessage latest = msgs.get(0);
                    dto.setLastMessageContent(latest.getContent());
                    dto.setLastMessageTime(latest.getSendTime());

                    // 【修复点】：Boolean 类型判断
                    long unread = msgs.stream()
                            .filter(msg -> msg.getReceiverId().equals(userId) && Boolean.FALSE.equals(msg.getIsRead()))
                            .count();
                    dto.setUnreadCount((int)unread);
                }
            } else {
                dto.setLastMessageContent("暂无消息");
                dto.setUnreadCount(0);
            }
            sessions.add(dto);
        }

        return sessions;
    }

}