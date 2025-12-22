package com.bjfu.cms.mapper;

import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.Date;
import java.util.List;

@Mapper
public interface EditorMapper {
    // 1. 获取分配给我的稿件
    List<Manuscript> selectMyManuscripts(@Param("editorId") Integer editorId);

    // 2. 批量插入审稿任务 (注意 4 个参数)
    void batchInsertReviews(
            @Param("msId") Integer msId,
            @Param("rIds") List<Integer> rIds,
            @Param("inviteDate") Date inviteDate,
            @Param("deadline") Date deadline
    );

    // 3. 通用状态更新 (解决数据库 CHECK 约束)
    void updateManuscriptStatus(
            @Param("msId") Integer msId,
            @Param("status") String status,
            @Param("subStatus") String subStatus
    );
    List<User> selectUsersByRole(@Param("role") String role);
    // 4. 提交建议给主编
    void updateRecommendation(Manuscript manuscript);
}