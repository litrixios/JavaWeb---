package com.bjfu.cms.mapper;

import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface EditorMapper {
    // 1. 获取分配给我的稿件
    List<Manuscript> selectMyManuscripts(@Param("editorId") Integer editorId);

    Integer findUserByRole(@Param("role") String role);

    // 获取逾期的审稿任务列表
    List<Map<String, Object>> selectOverdueReviews();

    // 根据ID获取具体的审稿任务详情（包含审稿人邮箱）
        Map<String, Object> selectReviewDetailForRemind(@Param("reviewId") Integer reviewId);

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
    List<Map<String, Object>> selectOverdueSevenDays();
}