package com.bjfu.cms.controller;

import com.bjfu.cms.common.result.Result;
import com.bjfu.cms.entity.News;
import com.bjfu.cms.entity.File;
import com.bjfu.cms.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;

@Tag(name = "新闻管理接口")
@RestController
@RequestMapping("/api/news")
public class NewsController {

    @Autowired
    private NewsService newsService;

    @Operation(summary = "获取新闻列表，支持搜索和筛选")
    @GetMapping("/list")
    public Result<List<News>> getNewsList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(newsService.getAllNews(keyword, startDate, endDate));
    }

    @Operation(summary = "获取新闻详情")
    @GetMapping("/{id}")
    public Result<News> getNewsDetail(@PathVariable("id") Integer newsId) {
        return Result.success(newsService.getNewsById(newsId));
    }

    @Operation(summary = "新增新闻")
    @PostMapping("/add")
    public Result<Integer> addNews(@RequestBody News news) {
        return newsService.addNews(news);
    }

    @Operation(summary = "更新新闻")
    @PutMapping("/update")
    public Result<String> updateNews(@RequestBody News news) {
        return newsService.updateNews(news);
    }

    @Operation(summary = "删除新闻")
    @DeleteMapping("/delete/{id}")
    public Result<String> deleteNews(@PathVariable("id") Integer newsId) {
        return newsService.deleteNews(newsId);
    }

    @Operation(summary = "上传新闻附件")
    @PostMapping("/upload-file/{newsId}")
    public Result<String> uploadNewsFile(
            @PathVariable Integer newsId,
            @RequestParam("file") MultipartFile file) {
        return newsService.uploadNewsFile(newsId, file);
    }

    @Operation(summary = "获取新闻附件列表")
    @GetMapping("/files/{newsId}")
    public Result<List<File>> getNewsFiles(@PathVariable Integer newsId) {
        return Result.success(newsService.getFilesByNewsId(newsId));
    }

    @Operation(summary = "删除新闻附件")
    @DeleteMapping("/files/delete/{fileId}")
    public Result<String> deleteNewsFile(@PathVariable Integer fileId) {
        return newsService.deleteNewsFile(fileId);
    }

    @Operation(summary = "下载新闻附件")
    @GetMapping("/files/download/{fileId}")
    public ResponseEntity<Resource> downloadNewsFile(@PathVariable Integer fileId) {
        Result<Resource> result = newsService.downloadNewsFile(fileId);
        if (result.isSuccess()) {
            Resource resource = result.getData();
            String fileName = resource.getFilename();
            try {
                // 处理文件名编码，避免中文乱码
                String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name())
                        .replaceAll("\\+", "%20");

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename*=UTF-8''" + encodedFileName)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                        .body(resource);
            } catch (UnsupportedEncodingException e) {
                return ResponseEntity.internalServerError().build();
            }
        } else {
            return ResponseEntity.badRequest().body(null);
        }
    }
}