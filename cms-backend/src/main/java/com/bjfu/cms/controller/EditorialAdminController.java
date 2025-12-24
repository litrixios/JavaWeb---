package com.bjfu.cms.controller;

import com.bjfu.cms.common.result.Result;
import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.dto.TechCheckAnalysisDTO;
import com.bjfu.cms.entity.dto.TechCheckDTO;
import com.bjfu.cms.service.EditorialAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/editorial-admin")
@Tag(name = "编辑部管理员功能模块", description = "编辑部管理员专用的接口")
public class EditorialAdminController {

    @Autowired
    private EditorialAdminService editorialAdminService;

    @Operation(summary = "获取待形式审查的稿件列表")
    @GetMapping("/manuscripts/tech-check")
    public Result<List<Manuscript>> getTechCheckManuscripts() {
        List<Manuscript> manuscripts = editorialAdminService.getTechCheckManuscripts();
        return Result.success(manuscripts);
    }

    @Operation(summary = "形式审查")
    @PostMapping("/tech-check")
    public Result<String> techCheck(@RequestBody TechCheckDTO dto) {
        editorialAdminService.techCheck(dto);
        return Result.success(dto.getPassed() ? "形式审查通过" : "形式审查未通过，已退回给作者");
    }

    @Operation(summary = "退回修改")
    @PostMapping("/unsubmit")
    public Result<String> unsubmit(@RequestBody TechCheckDTO dto) {
        // 设置为未通过
        dto.setPassed(false);
        editorialAdminService.unsubmitManuscript(dto);
        return Result.success("稿件已退回给作者修改");
    }

    @Operation(summary = "获取形式审查文件分析（字数+查重）")
    @GetMapping("/tech-check/analysis")
    public Result<TechCheckAnalysisDTO> getTechCheckAnalysis(@RequestParam Integer manuscriptId) {
        // 服务层方法已返回 Result<TechCheckAnalysisDTO>，直接返回即可
        return editorialAdminService.getTechCheckFileAnalysis(manuscriptId);
    }

    @Operation(summary = "PDF在线预览（根据稿件最新版本OriginalFilePath，SFTP直出流）")
    @GetMapping("/manuscripts/pdf/preview")
    public void previewLatestPdf(@RequestParam Integer manuscriptId, HttpServletResponse response) {
        editorialAdminService.previewLatestPdf(manuscriptId, response);
    }

}