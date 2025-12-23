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

import java.net.URLEncoder;
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
        System.out.println(dto);
        if (dto.isAccepted()) {
            reviewerService.acceptInvitation(dto.getReviewId());
            return Result.success("审稿任务已接受，截止日期：" + reviewerService.getDeadline(dto.getReviewId()));
        } else {
            if (!dto.isAccepted()) {
                if (dto.getReason() == null || dto.getReason().trim().isEmpty()) {
                    return Result.error("拒绝审稿必须填写理由");
                }
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

            // 2. 获取文件名 (从路径中截取)
            String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);

            // 3. 设置响应头
            response.reset();
            response.setContentType("application/pdf"); // 如果确定是PDF，否则用 application/octet-stream
            response.setCharacterEncoding("utf-8");
            // 设置 Content-Disposition 让浏览器识别文件名
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));

            // 4. 调用文件服务将文件写入 Response 输出流
            sftpService.downloadToStream(filePath, response.getOutputStream());

            // 5. 记录下载日志（可选）
            // userMapper.insertLog(...)

        } catch (Exception e) {
            // 发生错误时，如果响应未提交，可以返回错误信息
            // 但由于是 void 方法且涉及流，通常只能打印日志或通过 Header 返回错误
            e.printStackTrace();
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