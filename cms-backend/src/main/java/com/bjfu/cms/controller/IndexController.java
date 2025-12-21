package com.bjfu.cms.controller;

import com.bjfu.cms.entity.EditorialBoard;
import com.bjfu.cms.entity.News;
import com.bjfu.cms.service.EditorialBoardService;
import com.bjfu.cms.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import java.util.Map;
import java.util.ArrayList; // 需要导入ArrayList

@Controller
public class IndexController {

    @Autowired
    private EditorialBoardService editorialBoardService;

    @Autowired
    private NewsService newsService;

    @GetMapping("/index")
    public String indexPage(Model model) {
        // 获取编委数据
        List<Map<String, Object>> editorialBoard = editorialBoardService.getPublicList();
        System.out.println("获取到的编委数据数量: " + editorialBoard.size());
        for(Map<String, Object> map : editorialBoard){
            System.out.println("编委信息: " + map);
        }
        model.addAttribute("editorialBoard", editorialBoard);

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

        return "index";
    }
}