package com.bjfu.cms.service.impl;

import com.bjfu.cms.common.utils.UserContext;
import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.Review;
import com.bjfu.cms.entity.User;
import com.bjfu.cms.entity.dto.ReviewSubmitDTO;
import com.bjfu.cms.mapper.ManuscriptMapper;
import com.bjfu.cms.mapper.ReviewMapper;
import com.bjfu.cms.mapper.UserMapper;
import com.bjfu.cms.service.CommunicationService;
import com.bjfu.cms.service.EmailService;
import com.bjfu.cms.service.ReviewerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Autowired
    private CommunicationService communicationService;

    @Value("${aliyun.ecs.remote-dir}")
    private String remoteBaseDir;  // 远程根目录
    @Value("${file.temp.path}")
    private String localTempPath;  // 本地临时目录



    @Override
    public List<Review> getMyReviews() {
        Integer reviewerId = UserContext.getUserId();
        // 查询状态为Accepted，Completed的任务
        return reviewMapper.selectMyReviews(reviewerId);
    }

    @Override
    public List<Review> getPendingInvitations() {
        // 筛选Accepted的任务
        Integer reviewerId = UserContext.getUserId();
        return reviewMapper.selectPendingInvitations(reviewerId);
    }

    @Override
    public Manuscript getManuscriptByReviewId(Integer reviewId) {

        Review review = reviewMapper.selectById(reviewId);

        // 获取稿件基本信息（仅包含摘要等可让审稿人查看的内容）
        return manuscriptMapper.selectManuscriptForReview(review.getManuscriptID());
    }

    @Override
    @Transactional
    public void acceptInvitation(Integer reviewId) {
        Integer reviewerId = UserContext.getUserId();

        int rows = reviewMapper.updateStatus(reviewId, reviewerId, "Accepted");
        if (rows == 0) {
            throw new RuntimeException("更新审稿状态失败，请重试");
        }

        Review review = reviewMapper.selectById(reviewId);

        String logDesc = String.format("审稿人ID=%d接受了稿件ID=%d的审稿邀请", reviewerId, review.getManuscriptID());
        userMapper.insertLog(
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()),
                reviewerId,
                "AcceptReviewInvitation",
                review.getManuscriptID(),
                logDesc
        );

        Manuscript m = manuscriptMapper.selectById(review.getManuscriptID());
        if (m != null && m.getCurrentEditorId() != null) {
            communicationService.sendMessage(
                    1,
                    m.getCurrentEditorId(),
                    "MS-" + m.getManuscriptId(),
                    "审稿邀请已接受",
                    "审稿人 (ID=" + reviewerId + ") 已接受审稿邀请。",
                    0
            );
        }

    }

    @Override
    @Transactional
    public void rejectInvitation(Integer reviewId, String reason) {
        Integer reviewerId = UserContext.getUserId();

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
                emailService.sendHtmlMail(editor.getEmail(), null, "审稿拒绝通知", "理由：" + reason);
            }

            communicationService.sendMessage(
                    1,
                    manuscript.getCurrentEditorId(),
                    "MS-" + manuscript.getManuscriptId(),
                    "审稿邀请被拒绝",
                    "审稿人 (ID=" + reviewerId + ") 拒绝了邀请。理由：" + reason,
                    0
            );
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

    @Override
    @Transactional
    public void submitReview(ReviewSubmitDTO dto) {
        Integer reviewerId = UserContext.getUserId();
        Review review = reviewMapper.selectById(dto.getReviewId());

        reviewMapper.updateReviewComments(dto);

        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
        reviewMapper.completeReview(dto.getReviewId(), now);

        // 插入日志
        userMapper.insertLog(now, reviewerId, "SubmitReview", review.getManuscriptID(), "审稿人提交了审稿意见");

        // 通知编辑
        Manuscript m = manuscriptMapper.selectById(review.getManuscriptID());
        if (m != null && m.getCurrentEditorId() != null) {
            communicationService.sendMessage(
                    1,
                    m.getCurrentEditorId(),
                    "MS-" + m.getManuscriptId(),
                    "审稿意见已提交",
                    "审稿人 (ID=" + reviewerId + ") 已提交审稿意见，请及时处理。",
                    0
            );
        }
    }

    @Override
    public String getAnonymousFilePath(Integer reviewId) {
        Review review = reviewMapper.selectById(reviewId);

        // 从Versions表获取最新匿名路径
        String path = manuscriptMapper.selectLatestAnonymousFilePath(review.getManuscriptID());
        if (path == null || path.isEmpty()) {
            throw new RuntimeException("未找到该稿件的匿名版本文件");
        }

        return path;
    }



}