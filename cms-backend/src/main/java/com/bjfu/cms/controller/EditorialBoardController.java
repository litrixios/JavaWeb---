package com.bjfu.cms.controller;

import com.bjfu.cms.common.result.Result;
import com.bjfu.cms.entity.EditorialBoard;
import com.bjfu.cms.service.EditorialBoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/board")
@Tag(name = "编委管理接口")
public class EditorialBoardController {

    @Autowired
    private EditorialBoardService boardService;

    @Operation(summary = "【前台】获取编委名单")
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getListForPublic() {
        return Result.success(boardService.getPublicList());
    }

    @Operation(summary = "【后台】获取所有编委")
    @GetMapping("/admin/all")
    public Result<List<Map<String, Object>>> getListForAdmin() {
        return Result.success(boardService.getAdminList());
    }

    @Operation(summary = "【后台】新增编委")
    @PostMapping("/admin/add")
    public Result<String> add(@RequestBody EditorialBoard board) {
        boardService.addMember(board);
        return Result.success("添加成功");
    }

    @Operation(summary = "【后台】修改编委信息")
    @PutMapping("/admin/update")
    public Result<String> update(@RequestBody EditorialBoard board) {
        boardService.updateMember(board);
        return Result.success("更新成功");
    }

    @Operation(summary = "【后台】切换显隐状态")
    @PatchMapping("/admin/toggle")
    public Result<String> toggle(@RequestParam Integer boardId, @RequestParam Boolean isVisible) {
        boardService.toggleVisibility(boardId, isVisible);
        return Result.success("状态已更新");
    }

    @Operation(summary = "【后台】删除编委")
    @DeleteMapping("/admin/delete/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        boardService.removeMember(id);
        return Result.success("已删除");
    }
}