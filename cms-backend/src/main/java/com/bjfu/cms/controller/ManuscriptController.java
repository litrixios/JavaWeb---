package com.bjfu.cms.controller;

import com.bjfu.cms.common.result.Result;
import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.dto.ManuscriptDTO;
import com.bjfu.cms.service.ManuscriptService;
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
    @GetMapping("/my-list")
    @Operation(summary = "获取我的稿件列表")
    public Result<List<Manuscript>> getMyList() {
        List<Manuscript> list = manuscriptService.getMyManuscripts();
        return Result.success(list);
    }
}