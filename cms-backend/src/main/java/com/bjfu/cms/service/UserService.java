package com.bjfu.cms.service;

import com.bjfu.cms.common.result.Result;
import com.bjfu.cms.entity.User;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
//import com.bjfu.cms.common.result;

public interface UserService {
    // 上传用户头像
    Result<String> uploadAvatar(Integer userId, MultipartFile file);

    Result<String> getAvatarUrl(Integer userId);

    ResponseEntity<byte[]> getAvatar(Integer userId);
    //public byte[] getAvatarBytes(Integer userId);
    // 新增方法：获取用户头像
    Result<String> updateProfile(Integer userId,  String fullName,
                               String email, String affiliation, String researchDirection);
}