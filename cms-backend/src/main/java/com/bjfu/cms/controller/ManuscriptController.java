package com.bjfu.cms.controller;

import com.bjfu.cms.common.result.Result;
import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.dto.ManuscriptDTO;
import com.bjfu.cms.entity.dto.ManuscriptTrackDTO;
import com.bjfu.cms.service.ManuscriptService;
import com.fasterxml.jackson.databind.ObjectMapper; // SpringBoot自带的JSON工具
import com.github.pagehelper.PageInfo;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.IOException;

/**
 * 使用原生 Servlet 实现稿件相关的 API 接口
 * 映射路径: /api/manuscript/*
 * 这样 /api/manuscript/submit, /api/manuscript/my-manuscripts 等都会进到这里
 */
@WebServlet("/api/manuscript/*")
public class ManuscriptController extends HttpServlet {

    private ManuscriptService manuscriptService;
    // Jackson 对象，用于 JSON 序列化和反序列化
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void init() throws ServletException {
        // 手动从 Spring 容器获取 Service Bean
        WebApplicationContext ctx = WebApplicationContextUtils
                .getRequiredWebApplicationContext(getServletContext());
        manuscriptService = ctx.getBean(ManuscriptService.class);
    }

    /**
     * 处理 GET 请求
     * 对应原 Controller 的 @GetMapping
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 设置响应类型为 JSON
        resp.setContentType("application/json;charset=UTF-8");

        // 获取子路径，例如 "/my-manuscripts" 或 "/track/123"
        String pathInfo = req.getPathInfo();

        try {
            if ("/my-manuscripts".equals(pathInfo)) {
                handleGetMyManuscripts(req, resp);
            } else if (pathInfo != null && pathInfo.startsWith("/track/")) {
                handleTrack(req, resp, pathInfo);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            writeJson(resp, Result.error("系统繁忙：" + e.getMessage()));
        }
    }

    /**
     * 处理 POST 请求
     * 对应原 Controller 的 @PostMapping
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String pathInfo = req.getPathInfo();

        try {
            if ("/submit".equals(pathInfo)) {
                handleSubmit(req, resp);
            } else if ("/submit-revision".equals(pathInfo)) {
                handleSubmitRevision(req, resp);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            writeJson(resp, Result.error("操作失败：" + e.getMessage()));
        }
    }

    // ================= 具体的业务处理方法 =================

    // 1. 获取我的稿件列表
    private void handleGetMyManuscripts(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 手动解析参数
        String status = req.getParameter("status");
        String substatus = req.getParameter("substatus");
        int pageNum = parseIntOrDefault(req.getParameter("pageNum"), 1);
        int pageSize = parseIntOrDefault(req.getParameter("pageSize"), 10);

        // 调用 Service
        PageInfo<Manuscript> pageInfo = manuscriptService.getManuscriptList(pageNum, pageSize, status, substatus);

        // 返回结果
        writeJson(resp, Result.success(pageInfo));
    }

    // 2. 追踪稿件 (/track/{id})
    private void handleTrack(HttpServletRequest req, HttpServletResponse resp, String pathInfo) throws IOException {
        // 解析路径中的 ID (例如 /track/5 -> 截取 5)
        String idStr = pathInfo.substring("/track/".length());
        Integer manuscriptId = Integer.valueOf(idStr);

        ManuscriptTrackDTO trackDTO = manuscriptService.trackManuscript(manuscriptId);
        writeJson(resp, Result.success(trackDTO));
    }

    // 3. 投稿或保存草稿 (POST /submit)
    private void handleSubmit(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 从 Request Body 读取 JSON 并转为 DTO
        ManuscriptDTO dto = objectMapper.readValue(req.getInputStream(), ManuscriptDTO.class);

        manuscriptService.submitManuscript(dto);

        String msg = "SUBMIT".equalsIgnoreCase(dto.getActionType()) ? "投稿成功" : "草稿已保存";
        writeJson(resp, Result.success(msg));
    }

    // 4. 提交修回 (POST /submit-revision)
    private void handleSubmitRevision(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ManuscriptDTO dto = objectMapper.readValue(req.getInputStream(), ManuscriptDTO.class);

        // 原 Controller 的参数校验逻辑
        if (dto.getManuscriptId() == null) {
            writeJson(resp, Result.error("稿件ID不能为空"));
            return;
        }
        if (dto.getMarkedFilePath() == null ||
                dto.getResponseLetterPath() == null ||
                dto.getAnonymousFilePath() == null) {
            writeJson(resp, Result.error("必须上传匿名稿(Anonymous)、标记修改版(Marked)和回复信(Response)"));
            return;
        }

        manuscriptService.submitRevision(dto);
        writeJson(resp, Result.success("修回版本提交成功，已通知编辑部"));
    }

    // ================= 工具方法 =================

    /**
     * 将 Result 对象转为 JSON 并写入响应流
     */
    private void writeJson(HttpServletResponse resp, Result<?> result) throws IOException {
        String json = objectMapper.writeValueAsString(result);
        resp.getWriter().write(json);
    }

    private int parseIntOrDefault(String val, int def) {
        if (val == null || val.isEmpty()) return def;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return def;
        }
    }
}