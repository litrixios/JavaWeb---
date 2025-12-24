package com.bjfu.cms.service.strategy;

import com.bjfu.cms.entity.User;
import org.springframework.stereotype.Component;

@Component("EditorInChief_STRATEGY")
public class EditorInChiefMessageStrategy extends AbstractMessageStrategy {

    @Override
    protected void checkPermission(User sender, User receiver, Integer msgType, String topic) {
        // 1. 系统通知 (Type 0): 主编权限较高，允许发送系统通知
        if (msgType == 0) {
            return;
        }

        // 2. 聊天消息 (Type 1): 必须关联稿件
        if (msgType == 1) {
            if (topic == null || !topic.startsWith("MS-")) {
                throw new RuntimeException("主编发送消息必须关联具体的稿件ID。");
            }
            // 主编可以与系统内任何角色沟通，不做接收者限制
        }
    }
}