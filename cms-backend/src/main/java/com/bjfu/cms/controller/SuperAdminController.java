package com.bjfu.cms.controller;

import com.bjfu.cms.common.result.Result;
import com.bjfu.cms.entity.User;
import com.bjfu.cms.entity.UserPermission;
import com.bjfu.cms.entity.dto.AdminUserDTO;
import com.bjfu.cms.entity.dto.UserCreateDTO;
import com.bjfu.cms.service.SuperAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/super-admin")
@Tag(name = "超级管理员模块")
public class SuperAdminController {

    @Autowired
    private SuperAdminService superAdminService;

    @GetMapping("/users")
    @Operation(summary = "获取所有用户列表", description = "超级管理员查看所有用户信息")
    public Result<List<User>> getUserList(AdminUserDTO queryDTO) {
        return superAdminService.getAllUsers(queryDTO);
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详细信息")
    public Result<User> getUserDetail(@PathVariable Integer userId) {
        return superAdminService.getUserDetail(userId);
    }

    @PostMapping("/users")
    @Operation(summary = "创建用户", description = "超级管理员创建新用户")
    public Result<String> createUser(@RequestBody UserCreateDTO userDTO) {
        return superAdminService.createUser(userDTO);
    }

    @PutMapping("/users/{userId}")
    @Operation(summary = "更新用户信息", description = "超级管理员更新用户基本信息")
    public Result<String> updateUser(@PathVariable Integer userId, @RequestBody User user) {
        user.setUserId(userId);
        return superAdminService.updateUser(user);
    }

    @PutMapping("/users/{userId}/permissions")
    @Operation(summary = "更新用户权限", description = "超级管理员更新用户权限")
    public Result<String> updateUserPermissions(@PathVariable Integer userId, @RequestBody UserPermission permission) {
        permission.setUserId(userId);
        return superAdminService.updateUserPermission(permission);
    }

    @PutMapping("/users/{userId}/status")
    @Operation(summary = "更新用户状态", description = "启用或禁用用户账号")
    public Result<String> updateUserStatus(@PathVariable Integer userId, @RequestParam Integer status) {
        return superAdminService.updateUserStatus(userId, status);
    }

    @DeleteMapping("/users/{userId}")
    @Operation(summary = "删除用户", description = "超级管理员删除用户账号")
    public Result<String> deleteUser(@PathVariable Integer userId) {
        return superAdminService.deleteUser(userId);
    }

    @GetMapping("/users/check-username")
    @Operation(summary = "检查用户名是否存在", description = "检查用户名是否已被使用")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        return superAdminService.checkUsernameExists(username);
    }

    @GetMapping("/ping")
    @Operation(summary = "Ping", description = "健康检查，返回 pong")
    public Result<String> ping() {
        return Result.success("pong");
    }
}