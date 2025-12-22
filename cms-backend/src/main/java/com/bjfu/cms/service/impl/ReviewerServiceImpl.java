package com.bjfu.cms.service.impl;

import com.bjfu.cms.common.utils.UserContext;
import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.Review;
import com.bjfu.cms.entity.User;
import com.bjfu.cms.mapper.ManuscriptMapper;
import com.bjfu.cms.mapper.ReviewMapper;
import com.bjfu.cms.mapper.UserMapper;
import com.bjfu.cms.service.EmailService;
import com.bjfu.cms.service.ReviewerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.List;

@Service
public class ReviewerServiceImpl implements ReviewerService {

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private ManuscriptMapper manuscriptMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EmailService emailService;


    @Override
    public List<Review> getPendingInvitations() {
        // 获取当前登录审稿人ID
        Integer reviewerId = UserContext.getUserId();
        return reviewMapper.selectPendingInvitations(reviewerId);
    }

    @Override
    public Manuscript getManuscriptByReviewId(Integer reviewId) {
        // 验证权限：确保该审稿任务属于当前登录用户
        Integer reviewerId = UserContext.getUserId();
        Review review = reviewMapper.selectById(reviewId);

        if (review == null || !review.getReviewerID().equals(reviewerId)) {
            throw new RuntimeException("无权访问该审稿任务");
        }

        // 获取稿件基本信息（仅包含摘要等可让审稿人查看的内容）
        return manuscriptMapper.selectManuscriptForReview(review.getManuscriptID());
    }

    @Override
    @Transactional
    public void acceptInvitation(Integer reviewId) {
        Integer reviewerId = UserContext.getUserId();

        // 更新审稿状态为已接受
        int rows = reviewMapper.updateStatus(reviewId, reviewerId, "Accepted");
        if (rows == 0) {
            throw new RuntimeException("更新审稿状态失败，请重试");
        }

        // 记录操作日志
        Review review = reviewMapper.selectById(reviewId);
        String logDesc = String.format("审稿人ID=%d接受了稿件ID=%d的审稿邀请", reviewerId, review.getManuscriptID());
        userMapper.insertLog(
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()),
                reviewerId,
                "AcceptReviewInvitation",
                review.getManuscriptID(),
                logDesc
        );
    }

    @Override
    @Transactional
    public void rejectInvitation(Integer reviewId, String reason) {
        Integer reviewerId = UserContext.getUserId();

        // 更新审稿状态为已拒绝
        int rows = reviewMapper.updateStatus(reviewId, reviewerId, "Rejected");
        if (rows == 0) {
            throw new RuntimeException("更新审稿状态失败，请重试");
        }

        // 记录拒绝理由
        reviewMapper.updateRejectionReason(reviewId, reason);

        // 记录操作日志
        Review review = reviewMapper.selectById(reviewId);
        String logDesc = String.format("审稿人ID=%d拒绝了稿件ID=%d的审稿邀请，理由：%s",
                reviewerId, review.getManuscriptID(), reason);
        userMapper.insertLog(
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()),
                reviewerId,
                "RejectReviewInvitation",
                review.getManuscriptID(),
                logDesc
        );

        Manuscript manuscript = manuscriptMapper.selectById(review.getManuscriptID());
        if (manuscript != null && manuscript.getCurrentEditorId() != null) {
            User editor = userMapper.selectById(manuscript.getCurrentEditorId());
            if (editor != null && editor.getEmail() != null) {
                // 发送邮件通知逻辑（可调用现有的邮件服务）
                String emailTitle = String.format("审稿邀请拒绝通知（稿件ID=%d）", review.getManuscriptID());
                String emailContent = String.format("审稿人ID=%d拒绝了稿件ID=%d的审稿邀请，理由：%s",
                        reviewerId, review.getManuscriptID(), reason);
                // 发送邮件通知逻辑（调用现有邮件服务）
                emailService.sendHtmlMail(editor.getEmail(), null, emailTitle, emailContent);
            }
        }
    }

    @Override
    public String getDeadline(Integer reviewId) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null || review.getDeadline() == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd").format(review.getDeadline());
    }



}