package com.bjfu.cms.controller;

import com.bjfu.cms.common.result.Result;
import com.bjfu.cms.entity.dto.EicDecisionDTO;
import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.service.EditorInChiefService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eic")
@Tag(name = "主编功能模块", description = "主编专用的稿件管理接口")
public class EditorInChiefController {

    @Autowired
    private EditorInChiefService eicService;


    @GetMapping("/manuscript/list")
    @Operation(summary = "全览稿件", description = "查看所有稿件，可选状态筛选")
    public Result<List<Manuscript>> getAllManuscripts(@RequestParam(required = false) String status) {
        // [cite: 263, 264] 主编可查看系统内所有稿件的状态、历史
        List<Manuscript> list = eicService.getAllManuscripts(status);
        return Result.success(list);
    }

    @PostMapping("/desk-review")
    @Operation(summary = "初审 (Desk Review)", description = "提交 DeskAccept 或 DeskReject")
    public Result<String> deskReview(@RequestBody EicDecisionDTO dto) {
        // [cite: 265] 主编对新投稿进行初步审查，决定直接拒稿或送审
        eicService.deskReview(dto);
        return Result.success("初审完成");
    }

    @PostMapping("/assign-editor")
    @Operation(summary = "指派编辑", description = "将稿件分配给 Associate Editor")
    public Result<String> assignEditor(@RequestBody EicDecisionDTO dto) {
        // [cite: 265] 主编将通过初审的稿件分配给特定编辑处理
        eicService.assignEditor(dto);
        return Result.success("编辑分配成功");
    }

    @PostMapping("/final-decision")
    @Operation(summary = "终审决策", description = "主编做出最终录用、拒稿或修改决定")
    public Result<String> finalDecision(@RequestBody EicDecisionDTO dto) {
        // [cite: 265] 主编基于编辑建议和审稿意见，做出最终决策
        eicService.makeFinalDecision(dto);
        return Result.success("最终决策已提交");
    }

    @PostMapping("/reviewer/invite")
    @Operation(summary = "邀请审稿人")
    public Result<String> inviteReviewer(@RequestBody EicDecisionDTO dto) {
        eicService.inviteReviewer(dto);
        return Result.success("邀请已发出");
    }

    @PostMapping("/reviewer/audit")
    @Operation(summary = "审核审稿人")
    public Result<String> auditReviewer(@RequestParam Integer userId, @RequestParam Integer status) {
        eicService.auditReviewer(userId, status);
        return Result.success("审核操作成功");
    }

    @PostMapping("/retract")
    @Operation(summary = "撤稿")
    public Result<String> retract(@RequestBody EicDecisionDTO dto) {
        eicService.retractManuscript(dto);
        return Result.success("稿件已撤回");
    }
}