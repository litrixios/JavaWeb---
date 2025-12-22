package com.bjfu.cms.controller;

import com.bjfu.cms.common.result.Result;
import com.bjfu.cms.entity.*;
import com.bjfu.cms.entity.dto.ReviewInviteDTO;
import com.bjfu.cms.service.EditorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "编辑业务接口")
@RestController
@RequestMapping("/api/editor")
public class EditorController {

    @Autowired
    private EditorService editorService;

    @Operation(summary = "查询指派给我的稿件")
    @GetMapping("/manuscripts")
    public Result<List<Manuscript>> getList() {
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
        // 接收 manuscriptId, editorRecommendation, editorSummaryReport
        editorService.submitToEIC(manuscript);
        return Result.success("总结报告已提交，等待主编审批");
    }
    @Operation(summary = "查询稿件详情")
    @GetMapping("/detail")
    public Result<Manuscript> getDetail(@RequestParam("id") Integer id) {
        // 调用 Service 获取单条稿件信息
        System.out.println(">>> 接收到前端详情请求 ID: " + id);
        Manuscript manuscript = editorService.getById(id);

        if (manuscript == null) {
            return Result.error("未找到该稿件详情");
        }
        return Result.success(manuscript);
    }
}