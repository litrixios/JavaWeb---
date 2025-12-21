package com.bjfu.cms.controller;

import com.bjfu.cms.common.result.Result;
import com.bjfu.cms.common.utils.JwtUtil;
import com.bjfu.cms.entity.User;
import com.bjfu.cms.entity.UserPermission;
import com.bjfu.cms.entity.dto.LoginDTO;
import com.bjfu.cms.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.bjfu.cms.entity.dto.RegisterDTO;

import java.util.Date;
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
       // System.out.println(loginDTO);
        User user = userMapper.findByUsername(loginDTO.getUsername());



        // 2. 校验用户是否存在
        if (user == null) {
            System.out.println("用户不存在");
            return Result.error("用户不存在");
        }
        if(!user.getRole().equals(loginDTO.getRole())){
            System.out.println("身份错误");
            return Result.error("身份错误");
        }

        if(!user.getRole().equals(loginDTO.getRole())){
            return Result.error("身份错误");
        }

        // 3. 校验密码 (课设初期用明文比对，后期可以用 BCryptPasswordEncoder)
        if (!user.getPassword().equals(loginDTO.getPassword())) {
            System.out.println("密码错误");
            return Result.error("密码错误");
        }

        // 4. 检查状态
        if (user.getStatus() == 2) {
            System.out.println("账号被禁用");
            return Result.error("账号已被禁用");
        }

        // 5. 生成 Token
        String token = JwtUtil.generateToken(user.getUserId(), user.getUsername(), user.getRole());

        // 6. 返回结果 (Token + 用户基本信息)
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user); // 前端需要用 user 里的 role 来控制路由
        System.out.println(data);
        return Result.success(data);
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody RegisterDTO registerDTO) {
        try {
            // 1. 检查用户名是否已存在
            User existingUser = userMapper.findByUsername(registerDTO.getUsername());
            if (existingUser != null) {
                return Result.error("用户名已存在");
            }

            // 2. 检查邮箱是否已存在
            if (userMapper.findByEmail(registerDTO.getEmail()) != null) {
                return Result.error("邮箱已被注册");
            }

            // 3. 创建新用户
            User newUser = new User();
            newUser.setUsername(registerDTO.getUsername());
            newUser.setPassword(registerDTO.getPassword()); // 实际项目中应该加密
            newUser.setEmail(registerDTO.getEmail());
            newUser.setFullName(registerDTO.getFullName());
            newUser.setRole(registerDTO.getRole()); // 设置用户角色
            newUser.setAffiliation(registerDTO.getAffiliation());
            newUser.setResearchDirection(registerDTO.getResearchDirection());
            newUser.setRegisterTime(new Date());
            newUser.setStatus(1); // 默认启用状态

            // 4. 插入用户到数据库
            userMapper.insertUser(newUser);

            // 5. 创建用户权限记录
            UserPermission permissions = new UserPermission();
            permissions.setUserId(newUser.getUserId());
            // 根据角色设置默认权限
            setDefaultPermissions(permissions, registerDTO.getRole());
            userMapper.insertUserPermissions(permissions);

            // 6. 生成Token并返回
            String token = JwtUtil.generateToken(newUser.getUserId(), newUser.getUsername(), newUser.getRole());

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", newUser);

            return Result.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("注册失败，请稍后重试");
        }
    }

    // 根据角色设置默认权限
    private void setDefaultPermissions(UserPermission permissions, String role) {
        switch (role) {
            case "Author":
                permissions.setCanSubmitManuscript(true);
                break;
            case "Reviewer":
                permissions.setCanWriteReview(true);
                break;
            default:
                break;
        }
    }
}