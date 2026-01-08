package com.bjfu.cms.servlet;

import com.bjfu.cms.entity.News;
import com.bjfu.cms.service.EditorialBoardService;
import com.bjfu.cms.service.NewsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// 映射路径：/hello（和原Controller一致）
@WebServlet("/submit")
public class SubmitServlet extends HttpServlet {
    private NewsService newsService;
    @Override
    public void init() throws ServletException {
        // 从Spring容器获取Bean
        WebApplicationContext ctx = WebApplicationContextUtils
                .getRequiredWebApplicationContext(getServletContext());
        newsService = ctx.getBean(NewsService.class);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. 替换原 model.addAttribute，往request域存数据
        //request.setAttribute("name", "张三");
        // 2. 获取激活的新闻列表
        List<News> allNews = newsService.getAllNews(null, null, null);
        List<News> newsList = new ArrayList<>();
        if (allNews != null) {
            for(News news : allNews) {
                if(news.getIsActive() != null && news.getIsActive()) {
                    newsList.add(news);
                }
            }
        }
        request.setAttribute("callForPapers", newsList);
        // 2. 转发到原 JSP 路径（和Spring Boot配置的前缀/后缀一致）
        request.getRequestDispatcher("/WEB-INF/jsp/submit.jsp").forward(request, response);
    }
}