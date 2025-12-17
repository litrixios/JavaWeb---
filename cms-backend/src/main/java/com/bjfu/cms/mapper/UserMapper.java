package com.bjfu.cms.mapper;

import com.bjfu.cms.entity.User;
import com.bjfu.cms.entity.UserPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    // 根据用户名查找用户（登录用）
    User findByUsername(@Param("username") String username);

    // 插入新用户
    int insertUser(User user);

    // 插入权限
    int insertPermissions(UserPermission permissions);


}