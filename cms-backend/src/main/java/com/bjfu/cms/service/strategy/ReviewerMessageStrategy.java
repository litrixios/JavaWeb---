package com.bjfu.cms.service.strategy;

import com.bjfu.cms.entity.User;
import org.springframework.stereotype.Component;

@Component("Reviewer_STRATEGY")
public class ReviewerMessageStrategy extends AbstractMessageStrategy {

    @Override
    protected void checkPermission(User sender, User receiver, Integer msgType, String topic) {
//        // 1. 审稿人无权发送系统通知 (Type 0)
//        // 业务层中若需通知编辑，应使用系统账号(ID=1)代发
//        if (msgType == 0) {
//            throw new RuntimeException("您在系统通知中无法发送内容");
//        }
//
//        // 2. 必须关联具体的稿件
//        if (topic == null || !topic.startsWith("MS-")) {
//            throw new RuntimeException("消息必须关联具体的稿件ID。");
//        }
//
//        // 3. 接收者必须是编辑侧人员 (Editor, EditorInChief, EditorialAdmin)
//        String role = receiver.getRole();
//        boolean isEditorSide = "Editor".equals(role) || "EditorInChief".equals(role) || "EditorialAdmin".equals(role);
//
//        if (!isEditorSide) {
//            throw new RuntimeException("审稿人只能与编辑人员进行沟通。");
//        }
    }
}