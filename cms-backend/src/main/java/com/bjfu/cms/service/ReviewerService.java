package com.bjfu.cms.service;

import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.Review;
import com.bjfu.cms.entity.dto.ReviewSubmitDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ReviewerService {


    /**
     * 获取审稿人已接受或已完成的审稿任务列表
     */
    List<Review> getMyReviews();

    /**
     * 获取当前审稿人待处理的邀请列表
     */
    List<Review> getPendingInvitations();

    /**
     * 根据审稿任务ID获取稿件详情
     */
    Manuscript getManuscriptByReviewId(Integer reviewId);

    /**
     * 接受审稿邀请
     */
    void acceptInvitation(Integer reviewId);

    /**
     * 拒绝审稿邀请
     */
    void rejectInvitation(Integer reviewId, String reason);

    /**
     * 获取审稿截止日期
     */
    String getDeadline(Integer reviewId);

    // 在 ReviewerService 接口中添加
    /**
     * 获取稿件文件路径（用于下载/预览）
     */
    String getManuscriptFilePath(Integer reviewId);

    /**
     * 提交最终审稿意见
     */
    void submitReview(ReviewSubmitDTO dto);

    /**
     * 获取允许审稿人下载的匿名文件路径
     * @param reviewId 审稿任务ID
     * @return 匿名PDF的文件路径
     */
    String getAnonymousFilePath(Integer reviewId);

}