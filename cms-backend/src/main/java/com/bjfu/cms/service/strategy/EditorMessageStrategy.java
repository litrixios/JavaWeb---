package com.bjfu.cms.service.strategy;

import com.bjfu.cms.entity.User;
import org.springframework.stereotype.Component;

// 1. 必须命名为 Editor_STRATEGY 以匹配数据库角色名 "Editor"
@Component("Editor_STRATEGY")
// 2. 必须继承 AbstractMessageStrategy，否则 saveMessage 等后续逻辑不会执行
public class EditorMessageStrategy extends AbstractMessageStrategy {

    @Override
    protected void checkPermission(User sender, User receiver) {
        // 获取接收者的角色
        String receiverRole = receiver.getRole();

        // 逻辑：编辑允许给作者 (Author) 或 主编 (EditorInChief) 发送消息
        boolean isValidReceiver = "Author".equals(receiverRole)
                || "EditorInChief".equals(receiverRole);

        if (!isValidReceiver) {
            throw new RuntimeException("编辑无权向该角色发送站内信。");
        }

        // 如果需要更严格的校验（例如：只能发给自己负责的稿件作者），
        // 可以在这里通过 sender.getUserId() 去数据库校验稿件所属权
    }
}