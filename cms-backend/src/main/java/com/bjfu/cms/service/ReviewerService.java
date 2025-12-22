package com.bjfu.cms.service;

import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.Review;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ReviewerService {

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

}