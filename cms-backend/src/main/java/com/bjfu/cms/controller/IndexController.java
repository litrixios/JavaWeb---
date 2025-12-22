package com.bjfu.cms.controller;

import com.bjfu.cms.entity.EditorialBoard;
import com.bjfu.cms.entity.File;
import com.bjfu.cms.entity.News;
import com.bjfu.cms.service.EditorialBoardService;
import com.bjfu.cms.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

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
        model.addAttribute("editorialBoard", editorialBoard);

        // 获取激活的新闻列表
        List<News> allNews = newsService.getAllNews(null, null, null);
        List<News> newsList = new ArrayList<>();
        if (allNews != null) {
            for(News news : allNews){
                if(news.getIsActive() != null && news.getIsActive()) {
                    newsList.add(news);
                }
            }
        }
        model.addAttribute("callForPapers", newsList);

        // 新增：获取每个新闻的附件信息
        List<Map<String, Object>> newsWithFiles = new ArrayList<>();
        for(News news : newsList) {
            try {
                // 获取新闻的附件列表
                List<File> files = newsService.getFilesByNewsId(news.getNewsId());
                Map<String, Object> newsMap = Map.of(
                        "newsId", news.getNewsId(),
                        "title", news.getTitle(),
                        "content", news.getContent(),
                        "publishDate", news.getPublishDate(),
                        "files", files != null ? files : new ArrayList<>()
                );
                newsWithFiles.add(newsMap);
            } catch (Exception e) {
                // 如果获取附件失败，仍然添加新闻信息，但附件列表为空
                Map<String, Object> newsMap = Map.of(
                        "newsId", news.getNewsId(),
                        "title", news.getTitle(),
                        "content", news.getContent(),
                        "publishDate", news.getPublishDate(),
                        "files", new ArrayList<>()
                );
                newsWithFiles.add(newsMap);
            }
        }
        model.addAttribute("newsWithFiles", newsWithFiles);

        return "index";
    }
}