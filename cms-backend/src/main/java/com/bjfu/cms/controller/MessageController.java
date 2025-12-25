package com.bjfu.cms.controller;

import com.bjfu.cms.common.result.Result;
import com.bjfu.cms.common.utils.UserContext;
import com.bjfu.cms.entity.InternalMessage;
import com.bjfu.cms.entity.dto.MessageSendDTO;
import com.bjfu.cms.service.CommunicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private CommunicationService communicationService;


    // 1. 发送接口 (统一入口，前端传 type)
    @PostMapping("/send")
    public Result<String> sendMessage(@RequestBody MessageSendDTO dto) {
        Integer senderId = UserContext.getUserId();
        // 默认认为是聊天(1)，除非前端显式传0
        Integer type = dto.getMsgType() != null ? dto.getMsgType() : 1;

        communicationService.sendMessage(
                senderId,
                dto.getReceiverId(),
                dto.getTopic(),
                dto.getTitle(),
                dto.getContent(),
                type
        );
        return Result.success("发送成功");
    }

    // 2. 获取【系统通知】列表 (用于右上角铃铛或通知中心)
    @GetMapping("/system-notifications")
    public Result<List<InternalMessage>> getSystemNotifications() {
        return Result.success(communicationService.getSystemNotifications());
    }

    // 3. 获取【特定稿件的聊天记录】 (用于聊天窗口)
    @GetMapping("/chat/{manuscriptId}")
    public Result<List<InternalMessage>> getChatHistory(@PathVariable Integer manuscriptId) {
        String topic = "MS-" + manuscriptId;
        return Result.success(communicationService.getChatHistory(topic));
    }

    @GetMapping("/list")
    public Result<List<InternalMessage>> getMyMessages() {
        return Result.success(communicationService.getMyAllMessages());
    }

    @GetMapping("/sessions")
    public Result<List<com.bjfu.cms.entity.dto.ChatSessionDTO>> getChatSessions() {
        Integer userId = UserContext.getUserId();
        // 确保 communicationService 中有这个方法
        return Result.success(communicationService.getChatSessions());
    }

}