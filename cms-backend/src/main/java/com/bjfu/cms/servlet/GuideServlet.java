package com.bjfu.cms.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

// 映射路径：/hello（和原Controller一致）
@WebServlet("/guide")
public class GuideServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. 替换原 model.addAttribute，往request域存数据
        request.setAttribute("name", "张三");

        // 2. 转发到原 JSP 路径（和Spring Boot配置的前缀/后缀一致）
        request.getRequestDispatcher("/WEB-INF/jsp/guide.jsp").forward(request, response);
    }
}