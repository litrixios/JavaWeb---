package com.bjfu.cms.service.strategy;

import com.bjfu.cms.entity.User;
import org.springframework.stereotype.Component;

// 注意：数据库 Role 是 "Author"，所以这里名字必须叫 "Author_STRATEGY"
@Component("Author_STRATEGY")
public class AuthorMessageStrategy extends AbstractMessageStrategy {
    @Override
    protected void checkPermission(User sender, User receiver) {
        String role = receiver.getRole(); // 这个 receiver 就是原消息的发件人

        // 逻辑：如果对方是编辑，允许发送（回复）
        boolean isEditor = "Editor".equals(role)
                || "EditorInChief".equals(role)
                || "EditorialAdmin".equals(role);

        // 逻辑：如果对方是审稿人，绝对禁止
        if ("Reviewer".equals(role)) {
            throw new RuntimeException("根据盲审原则，无法直接回复审稿人。");
        }

        // 逻辑：只允许回复给编辑部
        if (!isEditor) {
            // 这里可以根据需求放宽，比如是否允许回复管理员
            throw new RuntimeException("您只能回复编辑部工作人员的消息。");
        }
    }
}