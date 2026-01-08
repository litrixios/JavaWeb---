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

//    @Override
//    public void init() throws ServletException {
//        // 初始化时获取服务实例
//        editorialBoardService = ServiceFactory.getEditorialBoardService();
//        newsService = ServiceFactory.getNewsService();
//    }
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
            System.out.println("11111");
            List<Map<String, Object>> editorialBoard = editorialBoardService.getPublicList();

            request.setAttribute("editorialBoard", editorialBoard);

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

            // 3. 获取每个新闻的附件信息
            List<Map<String, Object>> newsWithFiles = new ArrayList<>();
            for(News news : newsList) {
                try {
                    // 获取新闻的附件列表
                    List<File> files = newsService.getFilesByNewsId(news.getNewsId());
                    Map<String, Object> newsMap = new HashMap<>();
                    newsMap.put("newsId", news.getNewsId());
                    newsMap.put("title", news.getTitle());
                    newsMap.put("content", news.getContent());
                    newsMap.put("publishDate", news.getPublishDate());
                    newsMap.put("files", files != null ? files : new ArrayList<>());
                    newsWithFiles.add(newsMap);
                } catch (Exception e) {
                    // 如果获取附件失败，仍然添加新闻信息，但附件列表为空
                    Map<String, Object> newsMap = new HashMap<>();
                    newsMap.put("newsId", news.getNewsId());
                    newsMap.put("title", news.getTitle());
                    newsMap.put("content", news.getContent());
                    newsMap.put("publishDate", news.getPublishDate());
                    newsMap.put("files", new ArrayList<>());
                    newsWithFiles.add(newsMap);
                    e.printStackTrace(); // 可记录日志
                }
            }
            request.setAttribute("newsWithFiles", newsWithFiles);

            // 4. 转发到JSP页面
            request.getRequestDispatcher("/WEB-INF/jsp/index.jsp").forward(request, response);

        } catch (Exception e) {
            // 错误处理
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误: " + e.getMessage());
        }
    }
}