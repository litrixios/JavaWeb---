package com.bjfu.cms.mapper;

import com.bjfu.cms.entity.User;
import com.bjfu.cms.entity.UserPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

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
    // 查询编辑列表（角色为Editor或AssociateEditor）
    List<User> selectEditors();

    // 根据角色查询用户
    List<User> selectByRole(@Param("role") String role);

    void insertLog(@Param("operationTime") String operationTime,
                   @Param("operatorId") Integer operatorId,
                   @Param("operationType") String operationType,
                   @Param("manuscriptId") Integer manuscriptId,
                   @Param("description") String description);



    // 根据邮箱查询用户
    User findByEmail(String email);
    // 插入用户权限
    void insertUserPermissions(UserPermission permissions);

    // 新增：查询所有审稿人
    List<User> selectReviewers();

    // 新增：统计稿件数量
    Integer countManuscriptsByStatus(@Param("status") String status);

    // 新增：查询稿件及其作者信息
    List<Map<String, Object>> selectManuscriptsWithAuthors(@Param("status") String status);
}