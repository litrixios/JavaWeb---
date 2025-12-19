package com.bjfu.cms.mapper;

import com.bjfu.cms.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {


    User findByUsername(@Param("username") String username);

    // 你之前定义的其他方法...
    void updateUserStatus(@Param("userId") Integer userId, @Param("status") Integer status);

    int insertUser(User user);

    void insertLog(@Param("operationTime") String operationTime,
                   @Param("operatorId") Integer operatorId,
                   @Param("operationType") String operationType,
                   @Param("manuscriptId") Integer manuscriptId,
                   @Param("description") String description);
}