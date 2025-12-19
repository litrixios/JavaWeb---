package com.bjfu.cms.service;

import com.bjfu.cms.entity.User;
import com.bjfu.cms.entity.UserPermission;
import com.bjfu.cms.entity.dto.AdminUserDTO;
import com.bjfu.cms.entity.dto.UserCreateDTO;
import com.bjfu.cms.common.result.Result;

import java.util.List;

public interface SuperAdminService {

    // 获取所有用户列表
    Result<List<User>> getAllUsers(AdminUserDTO queryDTO);

    // 获取用户详情
    Result<User> getUserDetail(Integer userId);

    // 创建用户
    Result<String> createUser(UserCreateDTO userDTO);

    // 更新用户信息
    Result<String> updateUser(User user);

    // 更新用户权限
    Result<String> updateUserPermission(UserPermission permission);

    // 更新用户状态
    Result<String> updateUserStatus(Integer userId, Integer status);

    // 删除用户
    Result<String> deleteUser(Integer userId);

    // 检查用户名是否存在
    Result<Boolean> checkUsernameExists(String username);
}