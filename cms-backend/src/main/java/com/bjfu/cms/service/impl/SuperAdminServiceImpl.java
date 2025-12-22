package com.bjfu.cms.service.impl;

import com.bjfu.cms.common.result.Result;
import com.bjfu.cms.entity.User;
import com.bjfu.cms.entity.UserPermission;
import com.bjfu.cms.entity.dto.AdminUserDTO;
import com.bjfu.cms.entity.dto.UserCreateDTO;
import com.bjfu.cms.mapper.SuperAdminMapper;
import com.bjfu.cms.mapper.UserMapper;
import com.bjfu.cms.service.SuperAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class SuperAdminServiceImpl implements SuperAdminService {

    @Autowired
    private SuperAdminMapper superAdminMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Result<List<User>> getAllUsers(AdminUserDTO queryDTO) {
        try {
            List<User> users = superAdminMapper.selectAllUsersWithPermissions(queryDTO);
            return Result.success(users);
        } catch (Exception e) {
            return Result.error("查询用户列表失败: " + e.getMessage());
        }
    }

    @Override
    public Result<User> getUserDetail(Integer userId) {
        try {
            User user = superAdminMapper.selectUserDetailById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            return Result.success(user);
        } catch (Exception e) {
            return Result.error("查询用户详情失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<String> createUser(UserCreateDTO userDTO) {
        try {
            // 检查用户名是否已存在
            int count = superAdminMapper.countByUsername(userDTO.getUsername());
            if (count > 0) {
                return Result.error("用户名已存在");
            }

            // 创建用户实体
            User user = new User();
            user.setUsername(userDTO.getUsername());
            user.setPassword(userDTO.getPassword()); // 注意：实际项目中应该加密
            user.setRole(userDTO.getRole());
            user.setEmail(userDTO.getEmail());
            user.setFullName(userDTO.getFullName());
            user.setAffiliation(userDTO.getAffiliation());
            user.setResearchDirection(userDTO.getResearchDirection());
            user.setStatus(userDTO.getStatus());
            user.setRegisterTime(new Date());

            // 使用原有的UserMapper插入用户
            int result = userMapper.insertUser(user);
            if (result <= 0) {
                return Result.error("创建用户失败");
            }

            // 创建默认权限
            UserPermission permissions = createDefaultPermissions(user.getUserId(), userDTO.getRole());
            userMapper.insertPermissions(permissions);

            return Result.success("用户创建成功");
        } catch (Exception e) {
            throw new RuntimeException("创建用户失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Result<String> updateUser(User user) {
        try {
            // 检查用户是否存在
            User existingUser = superAdminMapper.selectUserDetailById(user.getUserId());
            if (existingUser == null) {
                return Result.error("用户不存在");
            }

            int result = superAdminMapper.updateUserBasicInfo(user);
            if (result <= 0) {
                return Result.error("更新用户信息失败");
            }

            return Result.success("用户信息更新成功");
        } catch (Exception e) {
            throw new RuntimeException("更新用户信息失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Result<String> updateUserPermission(UserPermission permission) {
        try {
            // 检查用户是否存在
            User existingUser = superAdminMapper.selectUserDetailById(permission.getUserId());
            if (existingUser == null) {
                return Result.error("用户不存在");
            }

            int result = superAdminMapper.updateUserPermission(permission);
            if (result <= 0) {
                return Result.error("更新用户权限失败");
            }

            return Result.success("用户权限更新成功");
        } catch (Exception e) {
            throw new RuntimeException("更新用户权限失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Result<String> updateUserStatus(Integer userId, Integer status) {
        try {
            // 检查用户是否存在
            User existingUser = superAdminMapper.selectUserDetailById(userId);
            if (existingUser == null) {
                return Result.error("用户不存在");
            }

            int result = superAdminMapper.updateUserStatus(userId, status);
            if (result <= 0) {
                return Result.error("更新用户状态失败");
            }

            return Result.success("用户状态更新成功");
        } catch (Exception e) {
            return Result.error("更新用户状态失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<String> deleteUser(Integer userId) {
        try {
            // 检查用户是否存在
            User existingUser = superAdminMapper.selectUserDetailById(userId);
            if (existingUser == null) {
                return Result.error("用户不存在");
            }

            // 先删除权限
            superAdminMapper.deleteUserPermissions(userId);

            // 再删除用户
            int result = superAdminMapper.deleteUser(userId);
            if (result <= 0) {
                return Result.error("删除用户失败");
            }

            return Result.success("用户删除成功");
        } catch (Exception e) {
            throw new RuntimeException("删除用户失败: " + e.getMessage(), e);
        }
    }

        @Override
        public Result<Boolean> checkUsernameExists(String username) {
            try {
                int count = superAdminMapper.countByUsername(username);
                return Result.success(count > 0);
            } catch (Exception e) {
                return Result.error("检查用户名失败: " + e.getMessage());
            }
        }

    /**
     * 根据角色创建默认权限
     */
    private UserPermission createDefaultPermissions(Integer userId, String role) {
        UserPermission permissions = new UserPermission();
        permissions.setUserId(userId);

        // 根据角色设置默认权限
        switch (role) {
            case "Author":
                permissions.setCanSubmitManuscript(true);
                break;
            case "Reviewer":
                permissions.setCanWriteReview(true);
                break;
            case "Editor":
                permissions.setCanViewAllManuscripts(true);
                permissions.setCanAssignReviewer(true);
                permissions.setCanViewReviewerIdentity(true);
                break;
            case "EditorInchief":
                permissions.setCanViewAllManuscripts(true);
                permissions.setCanAssignReviewer(true);
                permissions.setCanViewReviewerIdentity(true);
                permissions.setCanMakeDecision(true);
                break;
            case "SystemAdmin":
                permissions.setCanViewAllManuscripts(true);
                permissions.setCanAssignReviewer(true);
                permissions.setCanViewReviewerIdentity(true);
                permissions.setCanModifySystemConfig(true);
                break;
            case "EditorialAdmin":
                permissions.setCanViewAllManuscripts(true);
                permissions.setCanTechCheck(true);
                permissions.setCanPublishNews(true);
                break;
            default:
                // 默认无权限
                break;
        }

        return permissions;
    }
}