package com.bjfu.cms.service.impl;

import com.bjfu.cms.common.result.Result;
import com.bjfu.cms.common.utils.UserContext;
import io.micrometer.common.util.internal.logging.InternalLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.bjfu.cms.entity.User;
import com.bjfu.cms.mapper.UserMapper;
import com.bjfu.cms.service.SftpService;
import com.bjfu.cms.service.UserService;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.UUID;
//import com.bjfu.cms.common.result;
//import com.bjfu.cms.common.UserContext; // 假设存在用户上下文工具类
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SftpService sftpService;


//    // 新增：获取用户头像（返回文件流）
//    @Override
//    public  ResponseEntity<byte[]> getAvatar(Integer userId) {
//        try {
//            // 1. 查询用户信息获取头像路径
//            User user = userMapper.selectById(userId);
//            if (user == null) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("用户不存在".getBytes());
//            }
//
//            // 2. 检查用户是否有头像
//            String avatarUrl = user.getAvatarUrl();
//
//            if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("用户未设置头像".getBytes());
//            }
//
//            // 3. 从SFTP服务器下载头像文件
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            sftpService.downloadToStream(avatarUrl, outputStream);
//            byte[] imageBytes = outputStream.toByteArray();
//
//            // 4. 根据文件扩展名设置Content-Type
//            String contentType = determineContentType(avatarUrl);
//
//            return ResponseEntity.ok()
//                    .contentType(MediaType.parseMediaType(contentType))
//                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"avatar\"")
//                    .body(imageBytes);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(("获取头像失败: " + e.getMessage()).getBytes());
//        }
//    }
//@Override
//public byte[] getAvatarBytes(Integer userId) {
//    try {
//        System.out.println("=== 开始获取用户头像 ===");
//        System.out.println("用户ID: " + userId);
//
//        User user = userMapper.selectById(userId);
//        if (user == null) {
//            throw new RuntimeException("用户不存在, ID: " + userId);
//        }
//
//        String avatarUrl = user.getAvatarUrl();
//        System.out.println("数据库中的头像路径: " + avatarUrl);
//
//        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
//            throw new RuntimeException("用户未设置头像");
//        }
//
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        sftpService.downloadToStream(avatarUrl, outputStream);
//        byte[] imageBytes = outputStream.toByteArray();
//        System.out.println("头像下载成功，文件大小: " + imageBytes.length + " bytes");
//
//        return imageBytes;
//    } catch (Exception e) {
//        System.out.println("获取头像失败: " + e.getMessage());
//        throw new RuntimeException("获取头像失败: " + e.getMessage());
//    }
//}
//    @Override
//    public ResponseEntity<byte[]> getAvatar(Integer userId) {
//        try {
//            System.out.println("=== 开始获取用户头像 ===");
//            System.out.println("用户ID: " + userId);
//
//            // 1. 查询用户信息获取头像路径
//            User user = userMapper.selectById(userId);
//            if (user == null) {
//                System.out.println("用户不存在, ID: " + userId);
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("用户不存在".getBytes());
//            }
//
//            // 2. 检查用户是否有头像
//            String avatarUrl = user.getAvatarUrl();
//            System.out.println("数据库中的头像路径: " + avatarUrl);
//
//            if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
//                System.out.println("用户未设置头像");
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("用户未设置头像".getBytes());
//            }
//
//            // 3. 从SFTP服务器下载头像文件
//            System.out.println("开始从SFTP下载头像: " + avatarUrl);
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            sftpService.downloadToStream(avatarUrl, outputStream);
//            byte[] imageBytes = outputStream.toByteArray();
//            System.out.println("头像下载成功，文件大小: " + imageBytes.length + " bytes");
//
//            // 4. 根据文件扩展名设置Content-Type
//            String contentType = determineContentType(avatarUrl);
//            System.out.println("设置Content-Type: " + contentType);
//
//            return ResponseEntity.ok()
//                    .contentType(MediaType.parseMediaType(contentType))
//                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"avatar\"")
//                    .body(imageBytes);
//
//        } catch (Exception e) {
//            System.out.println("获取头像失败: " + e.getMessage());
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(("获取头像失败: " + e.getMessage()).getBytes());
//        }
//    }
    @Override
    @Transactional
    public Result<String> updateProfile(Integer userId, String fullName,
                                      String email, String affiliation, String researchDirection) {
        //2. 查询用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        //3. 更新基本信息（非空才更新）
        user.setFullName(fullName);
        user.setEmail(email);
        user.setAffiliation(affiliation);
        user.setResearchDirection(researchDirection);

        // 5. 保存更新到数据库
        userMapper.updateById(user);
        // 6. 返回更新后的用户信息（注意脱敏，如隐藏密码）
        return Result.success("更新成功");
    }

    @Override
    public ResponseEntity<byte[]> getAvatar(Integer userId) {
        try {
            System.out.println("=== 开始获取用户头像 ===");
            System.out.println("用户ID: " + userId);

            // 1. 查询用户信息获取头像路径
            User user = userMapper.selectById(userId);
            if (user == null) {
                System.out.println("用户不存在, ID: " + userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("用户不存在".getBytes());
            }

            // 2. 检查用户是否有头像
            String avatarUrl = user.getAvatarUrl();
            System.out.println("数据库中的头像路径: " + avatarUrl);

            if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
                System.out.println("用户未设置头像");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("用户未设置头像".getBytes());
            }

            // 3. 从SFTP服务器下载头像文件
            System.out.println("开始从SFTP下载头像: " + avatarUrl);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            sftpService.downloadToStream(avatarUrl, outputStream);
            byte[] imageBytes = outputStream.toByteArray();
            System.out.println("头像下载成功，文件大小: " + imageBytes.length + " bytes");

            // 4. 根据文件扩展名设置Content-Type
            String contentType = determineContentType(avatarUrl);
            System.out.println("设置Content-Type: " + contentType);

            // 5. 获取文件名
            String fileName = getFileNameFromPath(avatarUrl);

            // 6. 设置下载响应头
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(imageBytes.length))
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .header(HttpHeaders.EXPIRES, "0")
                    .body(imageBytes);

        } catch (Exception e) {
            System.out.println("获取头像失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("获取头像失败: " + e.getMessage()).getBytes());
        }
    }

    // 新增辅助方法：从路径中提取文件名
    private String getFileNameFromPath(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return "avatar";
        }
        int lastSlashIndex = filePath.lastIndexOf("/");
        if (lastSlashIndex >= 0 && lastSlashIndex < filePath.length() - 1) {
            return filePath.substring(lastSlashIndex + 1);
        }
        return "avatar";
    }
    // 新增：获取用户头像URL
    @Override
    public Result<String> getAvatarUrl(Integer userId) {
        try {
            // 1. 查询用户信息
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            // 2. 检查用户是否有头像
            String avatarUrl = user.getAvatarUrl();
            if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
                return Result.error("路径为"+user+"<UNK>");
            }

            // 3. 直接返回SFTP服务器上的完整路径
            // 根据您的代码，avatarUrl已经是完整路径，如：/root/cms/files/avatars/1/1698765432100.jpg
            return Result.success(avatarUrl);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取头像URL失败: " + e.getMessage());
        }
    }

    // 辅助方法：根据文件路径判断Content-Type
    private String determineContentType(String filePath) {
        if (filePath.toLowerCase().endsWith(".png")) {
            return "image/png";
        } else if (filePath.toLowerCase().endsWith(".jpg") || filePath.toLowerCase().endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (filePath.toLowerCase().endsWith(".gif")) {
            return "image/gif";
        } else {
            return "application/octet-stream";
        }
    }



    @Override
    @Transactional
    public Result<String> uploadAvatar(Integer userId, MultipartFile file) {
        // 1. 校验文件
        if (file.isEmpty()) {
            return Result.error("请选择头像文件");
        }

        try {
            // 2. 校验用户存在性
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 3. 删除旧头像（如果存在）
            if (user.getAvatarUrl() != null) {
                sftpService.deleteRemoteFile(user.getAvatarUrl());
            }

            // 4. 调用SFTP服务上传新头像，返回远程路径
            String avatarUrl = sftpService.uploadAvatar(file.getInputStream(), userId);

            // 5. 更新数据库头像路径
            userMapper.updateAvatar(userId, avatarUrl);

            return Result.success(avatarUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("头像上传失败：" + e.getMessage());
        }
    }
}