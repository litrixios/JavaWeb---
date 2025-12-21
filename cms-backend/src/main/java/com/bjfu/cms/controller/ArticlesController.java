package com.bjfu.cms.controller;

//import com.bjfu.cms.entity.CallForPapers;
//import com.bjfu.cms.service.JournalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller // 注意：这里必须用 @Controller，不能用 @RestController
public class ArticlesController {

//    @Autowired
//    private JournalService journalService;

    @GetMapping("/articles")
    public String hello(Model model) {
        return "articles";
    }
}