package com.bjfu.cms.controller;

import com.bjfu.cms.common.result.Result;
import com.bjfu.cms.entity.dto.EicDecisionDTO;
import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.User;
import com.bjfu.cms.service.EditorInChiefService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletOutputStream;
import java.util.List;
import java.util.Map;

/**
 * 主编功能控制器
 * 提供主编专用的稿件管理REST接口
 * 包含全览稿件、初审、指派编辑、终审决策、审稿人管理和特殊权限等功能
 */
@RestController
@RequestMapping("/api/eic")
@Tag(name = "主编功能模块", description = "主编专用的稿件管理接口")
public class EditorInChiefController {

    @Autowired
    private EditorInChiefService eicService; // 主编业务服务

    // ================== 全览权限 ==================

    /**
     * 全览稿件
     * 查看所有稿件，支持按状态筛选
     * @param status 状态筛选条件（可选）
     * @return 稿件列表结果
     */
    @GetMapping("/manuscript/list")
    @Operation(summary = "全览稿件", description = "查看所有稿件，可选状态筛选")
    public Result<List<Manuscript>> getAllManuscripts(@RequestParam(required = false) String status) {
        List<Manuscript> list = eicService.getAllManuscripts(status);
        return Result.success(list);
    }

    /**
     * 稿件统计
     * 获取稿件状态统计信息，包括各状态稿件数量
     * @return 统计结果
     */
    @GetMapping("/manuscript/statistics")
    @Operation(summary = "稿件统计", description = "获取稿件状态统计信息")
    public Result<Map<String, Integer>> getManuscriptStatistics() {
        Map<String, Integer> stats = eicService.getManuscriptStatistics();
        return Result.success(stats);
    }

    /**
     * 导出报表
     * 导出指定时间范围的稿件处理报表为Excel文件
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param response HTTP响应对象
     */
    @GetMapping("/report/export")
    @Operation(summary = "导出报表", description = "导出指定时间范围的稿件处理报表")
    public void exportReport(@RequestParam String startDate,
                             @RequestParam String endDate,
                             HttpServletResponse response) {
        byte[] reportBytes = eicService.exportReport(startDate, endDate);

        // 设置响应头
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition",
                "attachment;filename=manuscript_report_" + startDate + "_to_" + endDate + ".xlsx");

        // 写入响应流
        try {
            ServletOutputStream out = response.getOutputStream();
            out.write(reportBytes);
            out.flush();
        } catch (Exception e) {
            throw new RuntimeException("导出报表失败", e);
        }
    }

    // ================== 初审功能 ==================

    /**
     * 初审 (Desk Review)
     * 对新投稿进行初步审查，决定直接拒稿或送审
     * @param dto 初审决策数据传输对象
     * @return 操作结果
     */
    @PostMapping("/desk-review")
    @Operation(summary = "初审 (Desk Review)", description = "提交 DeskAccept 或 DeskReject")
    public Result<String> deskReview(@RequestBody EicDecisionDTO dto) {
        eicService.deskReview(dto);
        return Result.success("初审完成");
    }

    /**
     * 批量初审
     * 批量进行初审操作，提高处理效率
     * @param dtos 初审决策数据传输对象列表
     * @return 操作结果
     */
    @PostMapping("/desk-review/batch")
    @Operation(summary = "批量初审", description = "批量进行初审操作")
    public Result<String> batchDeskReview(@RequestBody List<EicDecisionDTO> dtos) {
        eicService.batchDeskReview(dtos);
        return Result.success("批量初审完成");
    }

    // ================== 指派编辑功能 ==================

    /**
     * 获取编辑列表
     * 获取所有可指派的编辑列表
     * @return 编辑列表结果
     */
    @GetMapping("/editor/list")
    @Operation(summary = "获取编辑列表", description = "获取所有可指派的编辑列表")
    public Result<List<User>> getEditorList() {
        List<User> editorList = eicService.getEditorList();
        return Result.success(editorList);
    }

    /**
     * 按专长筛选编辑
     * 根据研究方向筛选合适的编辑
     * @param expertise 研究方向/专长
     * @return 符合条件的编辑列表
     */
    @GetMapping("/editor/expertise")
    @Operation(summary = "按专长筛选编辑", description = "根据研究方向筛选编辑")
    public Result<List<User>> getEditorsByExpertise(@RequestParam String expertise) {
        List<User> editors = eicService.getEditorsByExpertise(expertise);
        return Result.success(editors);
    }

    /**
     * 指派编辑
     * 将稿件分配给 Associate Editor
     * @param dto 指派编辑数据传输对象
     * @return 操作结果
     */
    @PostMapping("/assign-editor")
    @Operation(summary = "指派编辑", description = "将稿件分配给编辑")
    public Result<String> assignEditor(@RequestBody EicDecisionDTO dto) {
        eicService.assignEditor(dto);
        return Result.success("编辑分配成功");
    }

    // ================== 终审决策 ==================

    /**
     * 终审决策
     * 主编基于编辑建议和审稿意见，做出最终录用、拒稿或修改决定
     * @param dto 终审决策数据传输对象
     * @return 操作结果
     */
    @PostMapping("/final-decision")
    @Operation(summary = "终审决策", description = "主编做出最终录用、拒稿或修改决定")
    public Result<String> finalDecision(@RequestBody EicDecisionDTO dto) {
        eicService.makeFinalDecision(dto);
        return Result.success("最终决策已提交");
    }

    // ================== 审稿人管理 ==================

    /**
     * 获取审稿人列表
     * 获取所有审稿人信息，包括待审核的审稿人
     * @return 审稿人列表结果
     */
    @GetMapping("/reviewer/list")
    @Operation(summary = "获取审稿人列表", description = "获取所有审稿人")
    public Result<List<User>> getReviewerList() {
        List<User> reviewerList = eicService.getReviewerList();
        // 调试输出
        for(User user : reviewerList) {
            System.out.println(user);
            System.out.println('\n');
        }
        return Result.success(reviewerList);
    }

    /**
     * 邀请审稿人
     * 向系统添加新的审稿人账号
     * @param dto 审稿人邀请数据传输对象
     * @return 操作结果
     */
    @PostMapping("/reviewer/invite")
    @Operation(summary = "邀请审稿人")
    public Result<String> inviteReviewer(@RequestBody EicDecisionDTO dto) {
        eicService.inviteReviewer(dto);
        return Result.success("邀请已发出");
    }

    /**
     * 审核审稿人
     * 审批或拒绝审稿人加入申请
     * @param userId 用户ID
     * @param status 审核状态（1:通过，0:拒绝）
     * @return 操作结果
     */
    @PostMapping("/reviewer/audit")
    @Operation(summary = "审核审稿人")
    public Result<String> auditReviewer(@RequestParam Integer userId, @RequestParam Integer status) {
        eicService.auditReviewer(userId, status);
        return Result.success("审核操作成功");
    }

    /**
     * 移除审稿人
     * 将审稿人移出库（加入黑名单），需填写理由
     * @param userId 用户ID
     * @param reason 移除原因
     * @return 操作结果
     */
    @PostMapping("/reviewer/remove")
    @Operation(summary = "移除审稿人", description = "将审稿人移出库（加入黑名单），需填写理由")
    public Result<String> removeReviewer(@RequestParam Integer userId, @RequestParam String reason) {
        eicService.removeReviewer(userId, reason);
        return Result.success("审稿人已移除");
    }

    // ================== 特殊权限 ==================

    /**
     * 撤稿
     * 撤回已发布的稿件
     * @param dto 撤稿数据传输对象
     * @return 操作结果
     */
    @PostMapping("/retract")
    @Operation(summary = "撤稿")
    public Result<String> retract(@RequestBody EicDecisionDTO dto) {
        eicService.retractManuscript(dto);
        return Result.success("稿件已撤回");
    }

    /**
     * 撤销决定
     * 撤销之前的决策，将稿件状态改回上一阶段
     * @param manuscriptId 稿件ID
     * @param newStatus 新的状态
     * @param reason 撤销原因
     * @return 操作结果
     */
    @PostMapping("/rescind-decision")
    @Operation(summary = "撤销决定", description = "撤销之前的决策")
    public Result<String> rescindDecision(@RequestParam Integer manuscriptId,
                                          @RequestParam String newStatus,
                                          @RequestParam String reason) {
        eicService.rescindDecision(manuscriptId, newStatus, reason);
        return Result.success("决策已撤销");
    }
}