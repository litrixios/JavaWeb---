package com.bjfu.cms.mapper;

import com.bjfu.cms.entity.InternalMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface InternalMessageMapper {

    // 插入消息 (注意：XML中也需要同步修改以支持 msgType)
    int insertMessage(InternalMessage message);

    // 查询某人的所有消息 (旧接口，可能之后会少用)
    List<InternalMessage> selectByReceiverId(@Param("receiverId") Integer receiverId);

    // 标记某条消息为已读
    void markAsRead(@Param("messageId") Integer messageId);

    // 根据 Topic 和 用户 查询 (用于聊天记录)
    List<InternalMessage> selectByTopicAndUser(@Param("topic") String topic, @Param("userId") Integer userId);

    // 【新增】根据 消息类型 和 接收者 查询 (用于分离 系统通知 和 聊天列表)
    // msgType: 0=系统通知, 1=聊天消息
    List<InternalMessage> selectByTypeAndReceiver(@Param("msgType") Integer msgType, @Param("receiverId") Integer receiverId);
}