package com.bjfu.cms.controller;

import com.bjfu.cms.common.result.Result;
import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.dto.ManuscriptDTO;
import com.bjfu.cms.entity.dto.ManuscriptTrackDTO;
import com.bjfu.cms.service.ManuscriptService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manuscript")
@Tag(name = "稿件管理模块")
public class ManuscriptController {

    @Autowired
    private ManuscriptService manuscriptService;

    /**
     * 作者投稿或保存草稿
     */
    @PostMapping("/submit")
    @Operation(summary = "投稿/保存草稿", description = "actionType传'SUBMIT'为正式提交，'SAVE'为保存草稿")
    public Result<String> submit(@RequestBody ManuscriptDTO manuscriptDTO) {
        manuscriptService.submitManuscript(manuscriptDTO);
        return Result.success(
                "SUBMIT".equalsIgnoreCase(manuscriptDTO.getActionType()) ? "投稿成功" : "草稿已保存"
        );
    }

    /**
     * 获取我的稿件列表
     */
    @GetMapping("/my-manuscripts")
    @Operation(summary = "获取我的稿件列表")
    public Result<PageInfo<Manuscript>> getMyManuscripts(
            @RequestParam(required = false) String status, // 接收前端传来的大状态，如 'Processing'
            @RequestParam(required = false) String substatus,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        // 调用 Service
        return Result.success(manuscriptService.getManuscriptList(pageNum, pageSize, status, substatus));
    }

    // 在 ManuscriptController 类中添加：

    /**
     * 提交修回 (Revision)
     * 作者在收到 "Revise" 决定后，上传修改稿和回复信
     */
    @PostMapping("/submit-revision")
    @Operation(summary = "提交修回稿件", description = "必须包含 manuscriptId, originalFilePath, markedFilePath, responseLetterPath")
    public Result<String> submitRevision(@RequestBody ManuscriptDTO manuscriptDTO) {
        // 1. 基础校验
        if (manuscriptDTO.getManuscriptId() == null) {
            return Result.error("稿件ID不能为空");
        }

        // 2. 校验关键文件是否存在 (根据业务需求，修回通常必须要有回复信和标记版)
        if (manuscriptDTO.getMarkedFilePath() == null || manuscriptDTO.getResponseLetterPath() == null) {
            return Result.error("必须上传标记修改版(Marked Version)和回复信(Response Letter)");
        }

        // 3. 调用 Service
        manuscriptService.submitRevision(manuscriptDTO);

        return Result.success("修回版本提交成功，已通知编辑部");
    }

    @GetMapping("/track/{manuscriptId}")
    @Operation(summary = "追踪稿件状态", description = "获取稿件详情及历史操作日志")
    public Result<ManuscriptTrackDTO> track(@PathVariable Integer manuscriptId) {
        return Result.success(manuscriptService.trackManuscript(manuscriptId));
    }
}