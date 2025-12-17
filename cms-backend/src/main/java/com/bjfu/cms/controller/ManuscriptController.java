package com.bjfu.cms.controller;

import com.bjfu.cms.common.result.Result;
import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.service.ManuscriptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manuscript")
@Tag(name = "稿件管理模块") // 1. 给整个 Controller 起个名
public class ManuscriptController {

    @Autowired
    private ManuscriptService manuscriptService;

    /**
     * 作者投稿接口
     * POST /api/manuscript/submit
     */
    @PostMapping("/submit")
    @Operation(summary = "作者投稿接口", description = "作者提交新稿件，ID和状态由后端自动生成") // 2. 给接口写说明
    public Result<String> submit(@RequestBody Manuscript manuscript) {
        // 这里的代码非常干净，没有 token 校验逻辑，因为拦截器和 UserContext 帮你做完了
        manuscriptService.submitManuscript(manuscript);
        return Result.success("投稿成功");
    }

    /**
     * 获取我的稿件列表
     * GET /api/manuscript/my-list
     */
    @GetMapping("/my-list")
    public Result<List<Manuscript>> getMyList() {
        List<Manuscript> list = manuscriptService.getMyManuscripts();
        return Result.success(list);
    }
}