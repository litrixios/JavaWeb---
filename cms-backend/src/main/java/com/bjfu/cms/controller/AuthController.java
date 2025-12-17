package com.bjfu.cms.controller;

import com.bjfu.cms.common.result.Result;
import com.bjfu.cms.common.utils.JwtUtil;
import com.bjfu.cms.entity.User;
import com.bjfu.cms.entity.dto.LoginDTO;
import com.bjfu.cms.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO loginDTO) {
        // 1. 查询用户
        User user = userMapper.findByUsername(loginDTO.getUsername());

        // 2. 校验用户是否存在
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 3. 校验密码 (课设初期用明文比对，后期可以用 BCryptPasswordEncoder)
        if (!user.getPassword().equals(loginDTO.getPassword())) {
            return Result.error("密码错误");
        }

        // 4. 检查状态
        if (user.getStatus() == 2) {
            return Result.error("账号已被禁用");
        }

        // 5. 生成 Token
        String token = JwtUtil.generateToken(user.getUserId(), user.getUsername(), user.getRole());

        // 6. 返回结果 (Token + 用户基本信息)
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user); // 前端需要用 user 里的 role 来控制路由

        return Result.success(data);
    }
}