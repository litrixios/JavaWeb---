package com.bjfu.cms.controller;

import com.bjfu.cms.common.result.Result;
import com.bjfu.cms.common.utils.UserContext;
import com.bjfu.cms.entity.InternalMessage;
import com.bjfu.cms.service.CommunicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private CommunicationService communicationService;

    // 发送消息
    @PostMapping("/send")
    public Result<String> sendMessage(@RequestBody MessageSendDTO dto) {
        Integer senderId = UserContext.getUserId();
        communicationService.sendMessage(
                senderId,
                dto.getReceiverId(),
                dto.getTopic(),
                dto.getTitle(),
                dto.getContent()
        );
        return Result.success("发送成功");
    }

    // 查看列表
    @GetMapping("/list")
    public Result<List<InternalMessage>> getMyList() {
        return Result.success(communicationService.getMyMessages());
    }

    // DTO 内部类
    public static class MessageSendDTO {
        private Integer receiverId;
        private String topic;
        private String title;
        private String content;
        // Getters & Setters
        public Integer getReceiverId() { return receiverId; }
        public void setReceiverId(Integer receiverId) { this.receiverId = receiverId; }
        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}