package com.bjfu.cms.servlet;

import com.bjfu.cms.entity.News;
import com.bjfu.cms.entity.File;
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
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

@WebServlet("/index")
public class IndexServlet extends HttpServlet {

    private EditorialBoardService editorialBoardService;
    private NewsService newsService;

    @Override
    public void init() throws ServletException {
        // 从Spring容器获取Bean
        WebApplicationContext ctx = WebApplicationContextUtils
                .getRequiredWebApplicationContext(getServletContext());

        editorialBoardService = ctx.getBean(EditorialBoardService.class);
        newsService = ctx.getBean(NewsService.class);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            // 1. 获取编委数据
            List<Map<String, Object>> editorialBoard = editorialBoardService.getPublicList();
            request.setAttribute("editorialBoard", editorialBoard);

            // 2. 获取激活的新闻列表
            List<News> allNews = newsService.getAllNews(null, null, null);
            List<News> activeNewsList = new ArrayList<>();
            if (allNews != null) {
                for(News news : allNews) {
                    if(news.getIsActive() != null && news.getIsActive()) {
                        activeNewsList.add(news);
                    }
                }
            }

            // 3. 分离新闻：有附件的放在征稿通知，没有附件的放在新闻列表
            List<Map<String, Object>> newsWithFiles = new ArrayList<>(); // 征稿通知（有附件）
            List<Map<String, Object>> newsWithoutFiles = new ArrayList<>(); // 新闻列表（无附件）

            for(News news : activeNewsList) {
                try {
                    // 获取新闻的附件列表
                    List<File> files = newsService.getFilesByNewsId(news.getNewsId());
                    Map<String, Object> newsMap = new HashMap<>();
                    newsMap.put("newsId", news.getNewsId());
                    newsMap.put("title", news.getTitle());
                    newsMap.put("content", news.getContent());
                    newsMap.put("publishDate", news.getPublishDate());
                    newsMap.put("files", files != null ? files : new ArrayList<>());

                    // 根据是否有附件进行分类
                    if (files != null && !files.isEmpty()) {
                        newsWithFiles.add(newsMap); // 有附件 -> 征稿通知
                    } else {
                        newsWithoutFiles.add(newsMap); // 无附件 -> 新闻列表
                    }
                } catch (Exception e) {
                    // 如果获取附件失败，默认放在新闻列表（无附件）
                    Map<String, Object> newsMap = new HashMap<>();
                    newsMap.put("newsId", news.getNewsId());
                    newsMap.put("title", news.getTitle());
                    newsMap.put("content", news.getContent());
                    newsMap.put("publishDate", news.getPublishDate());
                    newsMap.put("files", new ArrayList<>());
                    newsWithoutFiles.add(newsMap);
                    e.printStackTrace(); // 可记录日志
                }
            }

            // 4. 设置请求属性
            request.setAttribute("newsWithFiles", newsWithFiles); // 征稿通知数据
            request.setAttribute("newsWithoutFiles", newsWithoutFiles); // 新闻列表数据

            // 5. 转发到JSP页面
            request.getRequestDispatcher("/WEB-INF/jsp/index.jsp").forward(request, response);

        } catch (Exception e) {
            // 错误处理
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误: " + e.getMessage());
        }
    }
}