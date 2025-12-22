package com.bjfu.cms.controller;

import com.bjfu.cms.common.result.Result;
import com.bjfu.cms.service.SftpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@RestController
@RequestMapping("/api/common")
public class CommonController {

    @Autowired
    private SftpService sftpService;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        try {
            // 1. 生成唯一文件名，防止重名覆盖
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFileName = UUID.randomUUID().toString() + extension;

            // 2. 定义远程存储路径 (根据你的 SftpService 配置)
            String remoteDir = "/root/cms/files"; // 需确保与 application.properties 中的 aliyun.ecs.remote-dir 一致

            // 3. 调用 SftpService 上传
            // 注意：需要确保 SftpService 有支持 InputStream 的 upload 方法
            sftpService.upload(file.getInputStream(), newFileName, remoteDir);

            // 4. 返回文件访问路径或文件名 (供前端保存到数据库)
            // 这里返回相对路径或完整文件名，视你后续下载逻辑而定
            return Result.success(remoteDir + "/" + newFileName);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
}