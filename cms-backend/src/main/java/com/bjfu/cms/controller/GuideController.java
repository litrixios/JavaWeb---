package com.bjfu.cms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // 注意：这里必须用 @Controller，不能用 @RestController
public class GuideController {

    @GetMapping("/guide")
    public String GuidePage(Model model) {
        model.addAttribute("name", "张三");
        // 返回字符串 "hello"，配合配置的前后缀，实际寻找 /WEB-INF/jsp/hello.jsp
        return "guide";
    }
}