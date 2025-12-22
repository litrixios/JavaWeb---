package com.bjfu.cms.mapper;

import com.bjfu.cms.entity.User;
import com.bjfu.cms.entity.UserPermission;
import com.bjfu.cms.entity.dto.AdminUserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SuperAdminMapper {

    //获取所有的用户信息
    List<User>getUserList(AdminUserDTO queryDTO);
    // 查询所有用户（包含权限信息）
    List<User> selectAllUsersWithPermissions(AdminUserDTO queryDTO);

    // 根据用户ID查询用户详情（包含权限）
    User selectUserDetailById(@Param("userId") Integer userId);

    // 查询用户名是否存在
    int countByUsername(@Param("username") String username);

    // 更新用户状态
    int updateUserStatus(@Param("userId") Integer userId, @Param("status") Integer status);

    // 删除用户
    int deleteUser(@Param("userId") Integer userId);

    // 删除用户权限
    int deleteUserPermissions(@Param("userId") Integer userId);

    // 更新用户基本信息
    int updateUserBasicInfo(User user);

    // 更新用户权限信息
    int updateUserPermission(UserPermission permission);
}