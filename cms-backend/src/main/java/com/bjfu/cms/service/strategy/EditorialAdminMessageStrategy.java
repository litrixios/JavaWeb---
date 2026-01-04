package com.bjfu.cms.service.strategy;

import com.bjfu.cms.entity.User;
import org.springframework.stereotype.Component;

@Component("EditorialAdmin_STRATEGY")
public class EditorialAdminMessageStrategy extends AbstractMessageStrategy {

    @Override
    protected void checkPermission(User sender, User receiver, Integer msgType, String topic) {
//        // 1. 系统通知 (Type 0): 允许发送（用于形式审查退回等通知）
//        if (msgType == 0) {
//            return;
//        }
//
//        // 2. 聊天消息 (Type 1)
//        if (msgType == 1) {
//            if (topic == null || !topic.startsWith("MS-")) {
//                throw new RuntimeException("消息必须关联具体的稿件ID。");
//            }
//            // 通常与作者或编辑沟通
//        }
    }
}