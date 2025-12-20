package com.bjfu.cms.mapper;

import com.bjfu.cms.entity.User;
import com.bjfu.cms.entity.UserPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {


    User findByUsername(@Param("username") String username);

    // 【新增】根据ID查找用户（发送邮件用）
    User selectById(@Param("userId") Integer userId);

    // 插入新用户
    // 你之前定义的其他方法...
    void updateUserStatus(@Param("userId") Integer userId, @Param("status") Integer status);

    int insertUser(User user);

    // 插入权限
    int insertPermissions(UserPermission permissions);


    void insertLog(@Param("operationTime") String operationTime,
                   @Param("operatorId") Integer operatorId,
                   @Param("operationType") String operationType,
                   @Param("manuscriptId") Integer manuscriptId,
                   @Param("description") String description);
}