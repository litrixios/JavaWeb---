package com.bjfu.cms.mapper;

import com.bjfu.cms.entity.Review;
import com.bjfu.cms.entity.dto.ReviewSubmitDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReviewMapper {

    /**
     * 查询审稿人待处理的邀请
     */
    List<Review> selectPendingInvitations(@Param("reviewerId") Integer reviewerId);

    /**
     * 根据ID查询审稿任务
     */
    Review selectById(@Param("reviewId") Integer reviewId);

    /**
     * 更新审稿状态
     */
    int updateStatus(@Param("reviewId") Integer reviewId,
                     @Param("reviewerId") Integer reviewerId,
                     @Param("status") String status);

    /**
     * 更新拒绝理由
     */
    void updateRejectionReason(@Param("reviewId") Integer reviewId, @Param("reason") String reason);

// 在 ReviewMapper 接口中添加
    /**
     * 提交审稿意见
     */
    int updateReviewComments(ReviewSubmitDTO dto);

    /**
     * 更新审稿任务状态为已完成
     */
    int completeReview(@Param("reviewId") Integer reviewId, @Param("finishDate") String finishDate);
}