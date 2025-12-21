package com.bjfu.cms.controller;

//import com.bjfu.cms.entity.CallForPapers;
//import com.bjfu.cms.service.JournalService;
import com.bjfu.cms.entity.News;
import com.bjfu.cms.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller // 注意：这里必须用 @Controller，不能用 @RestController
public class SubmitController {

//    @Autowired
//    private JournalService journalService;

    @Autowired
    private NewsService newsService;

    @GetMapping("submit")
    public String SumitPage(Model model) {
        // 修复新闻数据处理逻辑
        List<News> allNews = newsService.getAllNews(null, null, null);
        List<News> newsList = new ArrayList<>(); // 初始化为空的ArrayList

        if (allNews != null) {
            for(News news : allNews){
                if(news.getIsActive() != null && news.getIsActive()) {
                    newsList.add(news);
                }
            }
        }

        System.out.println("获取到的新闻数据数量: " + (allNews != null ? allNews.size() : 0));
        System.out.println("激活的新闻数量: " + newsList.size());

        model.addAttribute("callForPapers", newsList);
        return "submit";
    }
}