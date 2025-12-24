package com.bjfu.cms.service.strategy;
import com.bjfu.cms.entity.User;
import org.springframework.stereotype.Component;

@Component("Author_STRATEGY")
public class AuthorMessageStrategy extends AbstractMessageStrategy {

    @Override
    protected void checkPermission(User sender, User receiver, Integer msgType, String topic) {
        // 1. 作者无权发送系统通知 (Type 0)
        if (msgType == 0) {
            throw new RuntimeException("您在系统通知中无法发送内容");
        }

        // 2. 必须关联具体的稿件 (Topic 格式校验)
        if (topic == null || !topic.startsWith("MS-")) {
            throw new RuntimeException("聊天必须关联具体的稿件ID。");
        }

        // 3. 接收者必须是编辑侧人员
        String role = receiver.getRole();
        boolean isEditor = "Editor".equals(role) || "EditorInChief".equals(role) || "EditorialAdmin".equals(role);

        if (!isEditor) {
            throw new RuntimeException("您只能与编辑人员进行沟通。");
        }
    }
}