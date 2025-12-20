package com.bjfu.cms.mapper;

import com.bjfu.cms.entity.InternalMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface InternalMessageMapper {

    // 插入消息
    int insertMessage(InternalMessage message);

    // 查询某人的所有消息
    List<InternalMessage> selectByReceiverId(@Param("receiverId") Integer receiverId);

    // 标记某条消息为已读
    void markAsRead(@Param("messageId") Integer messageId);

    // 查询某人的未读消息数量
    int countUnread(@Param("receiverId") Integer receiverId);
}