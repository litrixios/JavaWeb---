package com.bjfu.cms.controller;

import com.bjfu.cms.common.result.Result;
import com.bjfu.cms.mapper.EditorMapper;
import com.bjfu.cms.service.EditorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/editor/monitoring") // 独立的模块路径
public class MonitoringController {

    @Autowired
    private EditorService editorService;

    @Autowired
    private EditorMapper editorMapper;

    @GetMapping("/overdue")
    public Result getOverdueReviews() {
        // 调用你之前定义的 selectOverdueReviews SQL
        return Result.success(editorMapper.selectOverdueReviews());
    }

    @PostMapping("/remind")
    public Result remindReviewer(@RequestBody Map<String, Object> params) {
        // 从 params 获取 reviewId。注意前端传参类型
        Integer reviewId = (Integer) params.get("reviewId");
        String content = (String) params.get("content");

        if (reviewId == null || content == null) {
            return Result.error("缺少必要参数");
        }

        // 调用实现类逻辑
        editorService.sendRemindMail(reviewId, content);
        return Result.success("催审指令已发出");
    }
}