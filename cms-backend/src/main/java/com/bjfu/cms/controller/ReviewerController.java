package com.bjfu.cms.controller;

import com.bjfu.cms.common.result.Result;
import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.Review;
import com.bjfu.cms.entity.dto.ReviewInvitationResponseDTO;
import com.bjfu.cms.entity.dto.ReviewSubmitDTO;
import com.bjfu.cms.service.ReviewerService;
import com.bjfu.cms.service.SftpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "审稿人功能模块", description = "审稿人专用的接口")
@RestController
@RequestMapping("/api/reviewer")
public class ReviewerController {

    @Autowired
    private ReviewerService reviewerService;

    @Autowired
    private SftpService sftpService;

    @Value("${aliyun.ecs.remote-dir}")
    private String remoteBaseDir;  // 远程根目录
    @Value("${file.temp.path}")
    private String localTempPath;  // 本地临时目录



    @Operation(summary = "获取我的审稿任务列表（已接受/已完成）")
    @GetMapping("/my-reviews")
    public Result<List<Review>> getMyReviews() {
        return Result.success(reviewerService.getMyReviews());
    }


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


    @Operation(summary = "下载/预览匿名稿件文件")
    @GetMapping("/manuscript/download/{reviewId}")
    public void downloadAnonymousManuscript(@PathVariable Integer reviewId, HttpServletResponse response) {
        try {
            // 1. 获取匿名文件路径
            String filePath = reviewerService.getAnonymousFilePath(reviewId);

            // 2. 调用文件服务下载文件
            // 假设 sftpService 提供 downloadFile(String path, HttpServletResponse response) 方法
            sftpService.download(filePath, localTempPath);

            // 3. 记录下载日志（加分项要求）
            // userMapper.insertLog(...)
        } catch (Exception e) {
            throw new RuntimeException("文件下载失败：" + e.getMessage());
        }
    }

    @Operation(summary = "提交审稿意见")
    @PostMapping("/submit-review")
    public Result<String> submitReview(@RequestBody ReviewSubmitDTO dto) {
        // 基本校验
        if (dto.getRecommendation() == null) {
            return Result.error("请选择总体建议（接受/修改/拒稿）");
        }
        reviewerService.submitReview(dto);
        return Result.success("审稿意见提交成功");
    }
}