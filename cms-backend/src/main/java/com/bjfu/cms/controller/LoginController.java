package com.bjfu.cms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginPage(Model model) {
        return "login";
    }

    @PostMapping("/login")
    public String loginProcess(
            @RequestParam String username,
            @RequestParam String password,
            Model model) {
        System.out.println(username);
        // 这里添加您的登录验证逻辑
        if ("admin".equals(username) && "123".equals(password)) {
            // 登录成功，重定向到主页或仪表板
            return "hello";
        } else {
            // 登录失败，返回错误信息
            model.addAttribute("error", "用户名或密码错误");
            return "login";
        }
    }
}