package com.bjfu.cms.controller;

import com.bjfu.cms.common.result.Result;
import com.bjfu.cms.entity.*;
import com.bjfu.cms.entity.dto.ReviewInviteDTO;
import com.bjfu.cms.entity.dto.ReviewTrackingDTO;
import com.bjfu.cms.service.EditorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
// ... 原有 import 保持不变
import com.bjfu.cms.entity.User;

@Tag(name = "编辑业务接口")
@RestController
@RequestMapping("/api/editor")
public class EditorController {

    @Autowired
    private EditorService editorService;


    // --- 新增：获取审稿人列表接口 ---
    @Operation(summary = "查询所有审稿人")
    @GetMapping("/reviewers")
    public Result<List<User>> getAllReviewers() {
        return Result.success(editorService.getAllReviewers());
    }

    @Operation(summary = "查询指派给我的稿件")
    @GetMapping("/manuscripts")
    public Result<List<Manuscript>> getList() {
        List<Manuscript> s=editorService.getAssignedList();

        return Result.success(editorService.getAssignedList());
    }

    @Operation(summary = "指派/邀请审稿人")
    @PostMapping("/invite")
    public Result<String> invite(@RequestBody ReviewInviteDTO dto) {
        editorService.inviteReviewers(dto.getManuscriptId(), dto.getReviewerIds());
        return Result.success("邀请成功，稿件已进入审稿阶段");
    }

    @Operation(summary = "提交建议给主编")
    @PostMapping("/recommend")
    public Result<String> recommend(@RequestBody Manuscript manuscript) {
        editorService.submitToEIC(manuscript);
        return Result.success("总结报告已提交，等待主编审批");
    }

    @Operation(summary = "查询稿件详情")
    @GetMapping("/detail")
    public Result<Manuscript> getDetail(@RequestParam("id") Integer id) {
        Manuscript manuscript = editorService.getById(id);
        if (manuscript == null) return Result.error("未找到该稿件详情");
        return Result.success(manuscript);
    }

    @Operation(summary = "下载稿件附件")
    @GetMapping("/download/{id}")
    public void downloadManuscript(@PathVariable("id") Integer id, HttpServletResponse response) {
        try {
            // 直接调用 Service 即可，Service 内部会处理 response 的 Header 和 Body
            editorService.downloadManuscript(id, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // 智能推荐审稿人接口
    @GetMapping("/recommend")
    public Result<List<User>> getRecommendReviewers(@RequestParam("id") Integer manuscriptId) {
        // manuscriptId 对应前端传递的 id 参数
        List<User> list = editorService.recommendReviewers(manuscriptId);
        return Result.success(list);
    }
    // 2. 新增：获取评审跟踪列表（UnderReview + 聚合意见）
    @Operation(summary = "查询评审跟踪中的稿件")
    @GetMapping("/manuscripts/tracking")
    public Result<List<ReviewTrackingDTO>> getTrackingList(@RequestParam("editorId") Integer editorId) {
        // 前端直接在 URL 后面带上 ?editorId=xxx
        List<ReviewTrackingDTO> list = editorService.getReviewTrackingList(editorId);
        return Result.success(list);
    }

}

