package com.bjfu.cms.service.strategy;

import com.bjfu.cms.entity.User;
import org.springframework.stereotype.Component;

/**
 * 系统管理员消息策略
 * 对应数据库 Role = "SystemAdmin"
 */
@Component("SuperAdmin_STRATEGY")
public class SuperAdminMessageStrategy extends AbstractMessageStrategy {

    @Override
    protected void checkPermission(User sender, User receiver) {
        // 系统管理员拥有最高权限，可以向系统内任何用户（作者、编辑、审稿人等）发送通知
        // 这里的逻辑为空，表示不进行拦截，直接通过
    }
}