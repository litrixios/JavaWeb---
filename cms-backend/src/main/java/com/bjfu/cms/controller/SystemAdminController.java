package com.bjfu.cms.controller;

import com.bjfu.cms.common.result.Result;
import com.bjfu.cms.entity.User;
import com.bjfu.cms.entity.UserPermission;
import com.bjfu.cms.entity.dto.AdminUserDTO;
import com.bjfu.cms.entity.dto.RolePermissionDTO;
import com.bjfu.cms.entity.dto.UserCreateDTO;
import com.bjfu.cms.service.SuperAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system-admin")
@Tag(name = "系统管理员接口", description = "系统管理员相关功能")
public class SystemAdminController {

    @Autowired
    private SuperAdminService superAdminService;

    @GetMapping("/users")
    @Operation(summary = "获取用户列表", description = "根据条件查询用户列表")
    public Result<List<User>> getUserList(AdminUserDTO queryDTO) {
        return superAdminService.getAllUsers(queryDTO);
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详细信息")
    public Result<User> getUserDetail(@PathVariable Integer userId) {
        return superAdminService.getUserDetail(userId);
    }

    @PostMapping("/users")
    @Operation(summary = "创建用户", description = "创建新用户并设置默认权限")
    public Result<String> createUser(@RequestBody UserCreateDTO userDTO) {
        return superAdminService.createUser(userDTO);
    }

    @PutMapping("/users/{userId}")
    @Operation(summary = "更新用户信息", description = "更新用户基本信息")
    public Result<String> updateUser(@PathVariable Integer userId, @RequestBody User user) {
        user.setUserId(userId);
        return superAdminService.updateUser(user);
    }

    @PutMapping("/users/{userId}/status")
    @Operation(summary = "更新用户状态", description = "启用或禁用用户")
    public Result<String> updateUserStatus(@PathVariable Integer userId, @RequestBody Map<String, Integer> statusMap) {
        Integer status = statusMap.get("status");
        if (status == null) {
            return Result.error("状态参数不能为空");
        }
        return superAdminService.updateUserStatus(userId, status);
    }

    @DeleteMapping("/users/{userId}")
    @Operation(summary = "删除用户", description = "删除用户及其权限信息")
    public Result<String> deleteUser(@PathVariable Integer userId) {
        return superAdminService.deleteUser(userId);
    }

    @GetMapping("/users/check-username")
    @Operation(summary = "检查用户名是否存在", description = "检查用户名是否已被使用")
    public Result<Boolean> checkUsernameExists(@RequestParam String username) {
        return superAdminService.checkUsernameExists(username);
    }

    @GetMapping("/users/{userId}/permissions")
    @Operation(summary = "获取用户权限", description = "获取指定用户的权限信息")
    public Result<UserPermission> getUserPermissions(@PathVariable Integer userId) {
        try {
            Result<User> userResult = superAdminService.getUserDetail(userId);
            if (userResult.getCode() != 200 || userResult.getData() == null) {
                return Result.error("用户不存在");
            }
            User user = userResult.getData();
            return Result.success(user.getPermissions());
        } catch (Exception e) {
            return Result.error("获取用户权限失败: " + e.getMessage());
        }
    }

    @PutMapping("/users/permissions")
    @Operation(summary = "更新用户权限", description = "更新指定用户的权限设置")
    public Result<String> updateUserPermissions(@RequestBody UserPermission permission) {
        return superAdminService.updateUserPermission(permission);
    }

    @GetMapping("/role-permissions/{role}")
    @Operation(summary = "获取角色默认权限", description = "获取指定角色的默认权限配置")
    public Result<RolePermissionDTO> getRolePermissions(@PathVariable String role) {
        try {
            // 这里可以从数据库或配置文件中获取角色默认权限
            // 暂时返回空对象，由前端处理默认权限逻辑
            RolePermissionDTO rolePermission = new RolePermissionDTO();
            return Result.success(rolePermission);
        } catch (Exception e) {
            return Result.error("获取角色权限失败: " + e.getMessage());
        }
    }

    @PutMapping("/role-permissions/{role}")
    @Operation(summary = "更新角色默认权限", description = "更新指定角色的默认权限配置")
    public Result<String> updateRolePermissions(@PathVariable String role, @RequestBody RolePermissionDTO permissionDTO) {
        try {
            // 这里可以将角色默认权限保存到数据库或配置文件
            // 暂时返回成功
            return Result.success("角色权限更新成功");
        } catch (Exception e) {
            return Result.error("更新角色权限失败: " + e.getMessage());
        }
    }

    @GetMapping("/logs")
    @Operation(summary = "获取系统日志", description = "获取系统操作日志")
    public Result<List<Map<String, Object>>> getSystemLogs(
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        try {
            // 模拟日志数据 - 实际项目中应该从数据库查询
            List<Map<String, Object>> logs = List.of(
                    Map.of(
                            "logId", 1,
                            "operationTime", "2024-12-21 10:00:00",
                            "operatorName", "systemadmin",
                            "operationType", "Login",
                            "manuscriptId", "",
                            "description", "系统管理员登录系统"
                    ),
                    Map.of(
                            "logId", 2,
                            "operationTime", "2024-12-21 10:05:00",
                            "operatorName", "systemadmin",
                            "operationType", "User",
                            "manuscriptId", "",
                            "description", "查询用户列表"
                    )
            );

            return Result.success(logs);
        } catch (Exception e) {
            return Result.error("获取日志失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/logs")
    @Operation(summary = "清空系统日志", description = "清空所有系统操作日志")
    public Result<String> clearSystemLogs() {
        try {
            // 实际项目中应该删除数据库中的日志记录
            return Result.success("日志清空成功");
        } catch (Exception e) {
            return Result.error("清空日志失败: " + e.getMessage());
        }
    }

    @GetMapping("/system-info")
    @Operation(summary = "获取系统信息", description = "获取系统运行状态和统计信息")
    public Result<Map<String, Object>> getSystemInfo() {
        try {
            // 模拟系统信息
            Map<String, Object> systemInfo = Map.of(
                    "totalUsers", 150,
                    "activeUsers", 142,
                    "totalManuscripts", 89,
                    "pendingReviews", 23,
                    "systemVersion", "1.0.0",
                    "serverTime", System.currentTimeMillis(),
                    "databaseStatus", "正常",
                    "diskUsage", "45%"
            );

            return Result.success(systemInfo);
        } catch (Exception e) {
            return Result.error("获取系统信息失败: " + e.getMessage());
        }
    }
}