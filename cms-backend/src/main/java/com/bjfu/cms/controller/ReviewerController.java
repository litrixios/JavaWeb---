package com.bjfu.cms.controller;

import com.bjfu.cms.common.result.Result;
import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.Review;
import com.bjfu.cms.entity.dto.ReviewInvitationResponseDTO;
import com.bjfu.cms.service.ReviewerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "审稿人功能模块", description = "审稿人专用的接口")
@RestController
@RequestMapping("/api/reviewer")
public class ReviewerController {

    @Autowired
    private ReviewerService reviewerService;

    @Operation(summary = "查看待处理的审稿邀请列表")
    @GetMapping("/pending-invitations")
    public Result<List<Review>> getPendingInvitations() {
        return Result.success(reviewerService.getPendingInvitations());
    }

    @Operation(summary = "查看邀请的稿件详情")
    @GetMapping("/invitation/{reviewId}/manuscript")
    public Result<Manuscript> getManuscriptForReview(@PathVariable Integer reviewId) {
        return Result.success(reviewerService.getManuscriptByReviewId(reviewId));
    }

    @Operation(summary = "接受/拒绝审稿邀请")
    @PostMapping("/invitation/respond")
    public Result<String> respondToInvitation(@RequestBody ReviewInvitationResponseDTO dto) {
        if (dto.isAccepted()) {
            reviewerService.acceptInvitation(dto.getReviewId());
            return Result.success("审稿任务已接受，截止日期：" + reviewerService.getDeadline(dto.getReviewId()));
        } else {
            if (dto.getReason() == null || dto.getReason().trim().isEmpty()) {
                return Result.error("拒绝审稿必须填写理由");
            }
            reviewerService.rejectInvitation(dto.getReviewId(), dto.getReason());
            return Result.success("已拒绝审稿邀请");
        }
    }
}