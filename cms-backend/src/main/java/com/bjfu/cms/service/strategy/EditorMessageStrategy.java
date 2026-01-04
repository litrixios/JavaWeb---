package com.bjfu.cms.service.strategy;
import com.bjfu.cms.entity.User;
import org.springframework.stereotype.Component;

@Component("Editor_STRATEGY")
public class EditorMessageStrategy extends AbstractMessageStrategy {

    @Override
    protected void checkPermission(User sender, User receiver, Integer msgType, String topic) {
        // 聊天 (Type 1)
        if (msgType == 1) {
            if (!"Author".equals(receiver.getRole())) {
                throw new RuntimeException("编辑聊天对象仅限作者。");
            }
            if (topic == null || !topic.startsWith("MS-")) {
                throw new RuntimeException("编辑聊天必须绑定稿件ID。");
            }
        }

    }
}