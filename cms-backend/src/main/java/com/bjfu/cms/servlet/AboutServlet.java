package com.bjfu.cms.servlet;

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

@WebServlet("/about")
public class AboutServlet extends HttpServlet {
    private EditorialBoardService editorialBoardService;

    @Override
    public void init() throws ServletException {
        // 从Spring容器获取Bean
        WebApplicationContext ctx = WebApplicationContextUtils
                .getRequiredWebApplicationContext(getServletContext());

        editorialBoardService = ctx.getBean(EditorialBoardService.class);
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 复用原业务逻辑
        List<Map<String, Object>> editorialBoard = editorialBoardService.getPublicList();

        request.setAttribute("editorialBoard", editorialBoard);
        // 转发到about.jsp
        request.getRequestDispatcher("/WEB-INF/jsp/about.jsp").forward(request, response);
    }
}