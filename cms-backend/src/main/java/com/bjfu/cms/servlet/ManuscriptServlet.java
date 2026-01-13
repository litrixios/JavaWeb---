package com.bjfu.cms.servlet;

import com.bjfu.cms.common.result.Result;
import com.bjfu.cms.common.utils.JwtUtil;
import com.bjfu.cms.common.utils.UserContext;
import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.dto.ManuscriptDTO;
import com.bjfu.cms.entity.dto.ManuscriptTrackDTO;
import com.bjfu.cms.service.ManuscriptService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageInfo;
import io.jsonwebtoken.Claims;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * 稿件管理 Servlet
 * 替代原 ManuscriptController
 * 映射路径: /api/manuscript/*
 */
@WebServlet(urlPatterns = "/api/manuscript/*")
public class ManuscriptServlet extends HttpServlet {

    private ManuscriptService manuscriptService;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        // 1. 手动从 Spring 容器获取 Service 和 ObjectMapper
        WebApplicationContext ctx = WebApplicationContextUtils
                .getRequiredWebApplicationContext(getServletContext());
        this.manuscriptService = ctx.getBean(ManuscriptService.class);
        // 如果容器里没有配置 ObjectMapper，可以 new 一个: this.objectMapper = new ObjectMapper();
        // 但通常 Spring Boot 都有内置的
        try {
            this.objectMapper = ctx.getBean(ObjectMapper.class);
        } catch (Exception e) {
            this.objectMapper = new ObjectMapper();
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 设置响应类型为 JSON
        resp.setContentType("application/json;charset=UTF-8");

        // 2. 手动鉴权逻辑 (因为 Servlet 不走 SpringMVC 的拦截器)
        if (!authenticate(req, resp)) {
            return; // 鉴权失败，直接返回 401
        }

        try {
            String method = req.getMethod();
            String pathInfo = req.getPathInfo(); // 获取 /api/manuscript 之后的部分

            // 简单的路由分发
            if ("POST".equalsIgnoreCase(method)) {
                if ("/submit".equals(pathInfo)) {
                    handleSubmit(req, resp);
                } else if ("/submit-revision".equals(pathInfo)) {
                    handleSubmitRevision(req, resp);
                } else {
                    resp.setStatus(404);
                    writeJson(resp, Result.error("Endpoint not found"));
                }
            } else if ("GET".equalsIgnoreCase(method)) {
                if ("/my-manuscripts".equals(pathInfo)) {
                    handleGetMyManuscripts(req, resp);
                } else if (pathInfo != null && pathInfo.startsWith("/track/")) {
                    handleTrack(req, resp, pathInfo);
                } else {
                    resp.setStatus(404);
                    writeJson(resp, Result.error("Endpoint not found"));
                }
            } else {
                resp.setStatus(405); // Method Not Allowed
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            writeJson(resp, Result.error("Internal Server Error: " + e.getMessage()));
        } finally {
            // 3. 非常重要：请求结束必须清理 UserContext，防止内存泄漏和线程污染
            UserContext.remove();
        }
    }

    /**
     * 鉴权方法：解析 Header 中的 Token 并设置 UserContext
     */
    private boolean authenticate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // OPTIONS 请求直接放行 (CORS 预检)
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            return true;
        }

        String token = req.getHeader("Authorization");
        if (token != null && !token.isEmpty()) {
            Claims claims = JwtUtil.parseToken(token);
            if (claims != null) {
                Integer userId = (Integer) claims.get("userId");
                UserContext.setUserId(userId);
                // 也可以设置角色: UserContext.setUserRole((String) claims.get("role"));
                return true;
            }
        }

        // 鉴权失败返回 401
        resp.setStatus(401);
        writeJson(resp, Result.error("401" + "Unauthorized: Please Login"));
        return false;
    }

    /**
     * 处理投稿 (POST /submit)
     */
    private void handleSubmit(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 手动解析 JSON Body
        ManuscriptDTO manuscriptDTO = objectMapper.readValue(req.getInputStream(), ManuscriptDTO.class);

        manuscriptService.submitManuscript(manuscriptDTO);

        String msg = "SUBMIT".equalsIgnoreCase(manuscriptDTO.getActionType()) ? "投稿成功" : "草稿已保存";
        writeJson(resp, Result.success(msg));
    }

    /**
     * 处理提交修回 (POST /submit-revision)
     */
    private void handleSubmitRevision(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ManuscriptDTO manuscriptDTO = objectMapper.readValue(req.getInputStream(), ManuscriptDTO.class);

        if (manuscriptDTO.getManuscriptId() == null) {
            writeJson(resp, Result.error("稿件ID不能为空"));
            return;
        }
        if (manuscriptDTO.getMarkedFilePath() == null ||
                manuscriptDTO.getResponseLetterPath() == null ||
                manuscriptDTO.getAnonymousFilePath() == null) {
            writeJson(resp, Result.error("必须上传匿名稿(Anonymous)、标记修改版(Marked)和回复信(Response)"));
            return;
        }

        manuscriptService.submitRevision(manuscriptDTO);
        writeJson(resp, Result.success("修回版本提交成功，已通知编辑部"));
    }

    /**
     * 获取我的稿件 (GET /my-manuscripts)
     */
    private void handleGetMyManuscripts(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 手动解析 Query Params
        String status = req.getParameter("status");
        String subStatus = req.getParameter("substatus");

        int pageNum = 1;
        try {
            String pageNumStr = req.getParameter("pageNum");
            if (pageNumStr != null) pageNum = Integer.parseInt(pageNumStr);
        } catch (NumberFormatException e) { /* ignore */ }

        int pageSize = 10;
        try {
            String pageSizeStr = req.getParameter("pageSize");
            if (pageSizeStr != null) pageSize = Integer.parseInt(pageSizeStr);
        } catch (NumberFormatException e) { /* ignore */ }

        PageInfo<Manuscript> list = manuscriptService.getManuscriptList(pageNum, pageSize, status, subStatus);
        writeJson(resp, Result.success(list));
    }

    /**
     * 追踪稿件 (GET /track/{id})
     */
    private void handleTrack(HttpServletRequest req, HttpServletResponse resp, String pathInfo) throws IOException {
        // 解析路径参数 /track/123 -> 123
        try {
            String idStr = pathInfo.substring("/track/".length());
            Integer manuscriptId = Integer.parseInt(idStr);

            ManuscriptTrackDTO trackDTO = manuscriptService.trackManuscript(manuscriptId);
            writeJson(resp, Result.success(trackDTO));
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            writeJson(resp, Result.error("Invalid Manuscript ID"));
        }
    }

    /**
     * 辅助方法：输出 JSON 响应
     */
    private void writeJson(HttpServletResponse resp, Object data) throws IOException {
        PrintWriter out = resp.getWriter();
        out.print(objectMapper.writeValueAsString(data));
        out.flush();
    }
}