package com.bjfu.cms.servlet;

import com.bjfu.cms.common.result.Result;
import com.bjfu.cms.common.utils.JwtUtil;
import com.bjfu.cms.common.utils.UserContext;
import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.User;
import com.bjfu.cms.entity.dto.EicDecisionDTO;
import com.bjfu.cms.entity.dto.ManuscriptDetailDTO;
import com.bjfu.cms.service.EditorInChiefService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * 主编功能 Servlet
 * 替代原 EditorInChiefController
 * 映射路径: /api/eic/*
 */
@WebServlet(urlPatterns = "/api/eic/*")
public class EditorInChiefServlet extends HttpServlet {

    private EditorInChiefService eicService;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        // 从 Spring 容器中获取 Service 和 ObjectMapper
        WebApplicationContext ctx = WebApplicationContextUtils
                .getRequiredWebApplicationContext(getServletContext());
        this.eicService = ctx.getBean(EditorInChiefService.class);
        try {
            this.objectMapper = ctx.getBean(ObjectMapper.class);
        } catch (Exception e) {
            this.objectMapper = new ObjectMapper();
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 设置默认响应类型 (部分接口如导出报表会覆盖此设置)
        if (resp.getContentType() == null) {
            resp.setContentType("application/json;charset=UTF-8");
        }

        // 鉴权
        if (!authenticate(req, resp)) {
            return;
        }

        try {
            String method = req.getMethod();
            String pathInfo = req.getPathInfo();

            if (pathInfo == null) {
                resp.setStatus(404);
                writeJson(resp, Result.error("Endpoint not found"));
                return;
            }

            if ("GET".equalsIgnoreCase(method)) {
                handleGetRequests(req, resp, pathInfo);
            } else if ("POST".equalsIgnoreCase(method)) {
                handlePostRequests(req, resp, pathInfo);
            } else {
                resp.setStatus(405); // Method Not Allowed
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            // 如果已经开始了流输出（如导出文件），这里写入JSON可能会报错，但在大多数API调用中是安全的
            if (!resp.isCommitted()) {
                writeJson(resp, Result.error("Internal Server Error: " + e.getMessage()));
            }
        } finally {
            // 清理 UserContext，防止内存泄漏
            UserContext.remove();
        }
    }

    private void handleGetRequests(HttpServletRequest req, HttpServletResponse resp, String pathInfo) throws IOException {
        // 路由匹配
        if ("/manuscript/list".equals(pathInfo)) {
            // 全览稿件
            String status = req.getParameter("status");
            List<Manuscript> list = eicService.getAllManuscripts(status);
            writeJson(resp, Result.success(list));

        } else if ("/manuscript/statistics".equals(pathInfo)) {
            // 稿件统计
            Map<String, Integer> stats = eicService.getManuscriptStatistics();
            writeJson(resp, Result.success(stats));

        } else if ("/report/export".equals(pathInfo)) {
            // 导出报表 (特殊处理：文件下载)
            handleExportReport(req, resp);

        } else if ("/editor/list".equals(pathInfo)) {
            // 获取编辑列表
            List<User> list = eicService.getEditorList();
            writeJson(resp, Result.success(list));

        } else if ("/editor/expertise".equals(pathInfo)) {
            // 按专长筛选编辑
            String expertise = req.getParameter("expertise");
            List<User> list = eicService.getEditorsByExpertise(expertise);
            writeJson(resp, Result.success(list));

        } else if ("/reviewer/list".equals(pathInfo)) {
            // 获取审稿人列表
            List<User> list = eicService.getReviewerList();
            writeJson(resp, Result.success(list));

        } else if (pathInfo.matches("/manuscript/\\d+/details")) {
            // 查看稿件详细历史: /manuscript/{id}/details
            handleGetManuscriptDetails(resp, pathInfo);

        } else {
            resp.setStatus(404);
            writeJson(resp, Result.error("GET Endpoint not found: " + pathInfo));
        }
    }

    private void handlePostRequests(HttpServletRequest req, HttpServletResponse resp, String pathInfo) throws IOException {
        // 路由匹配
        if ("/desk-review".equals(pathInfo)) {
            // 初审
            EicDecisionDTO dto = objectMapper.readValue(req.getInputStream(), EicDecisionDTO.class);
            eicService.deskReview(dto);
            writeJson(resp, Result.success("初审完成"));

        } else if ("/desk-review/batch".equals(pathInfo)) {
            // 批量初审
            List<EicDecisionDTO> dtos = objectMapper.readValue(req.getInputStream(), new TypeReference<List<EicDecisionDTO>>(){});
            eicService.batchDeskReview(dtos);
            writeJson(resp, Result.success("批量初审完成"));

        } else if ("/assign-editor".equals(pathInfo)) {
            // 指派编辑
            EicDecisionDTO dto = objectMapper.readValue(req.getInputStream(), EicDecisionDTO.class);
            eicService.assignEditor(dto);
            writeJson(resp, Result.success("编辑分配成功"));

        } else if ("/final-decision".equals(pathInfo)) {
            // 终审决策
            EicDecisionDTO dto = objectMapper.readValue(req.getInputStream(), EicDecisionDTO.class);
            eicService.makeFinalDecision(dto);
            writeJson(resp, Result.success("最终决策已提交"));

        } else if ("/reviewer/invite".equals(pathInfo)) {
            // 邀请审稿人
            EicDecisionDTO dto = objectMapper.readValue(req.getInputStream(), EicDecisionDTO.class);
            eicService.inviteReviewer(dto);
            writeJson(resp, Result.success("邀请已发出"));

        } else if ("/reviewer/audit".equals(pathInfo)) {
            // 审核审稿人
            Integer userId = parseInt(req.getParameter("userId"));
            Integer status = parseInt(req.getParameter("status"));
            eicService.auditReviewer(userId, status);
            writeJson(resp, Result.success("审核操作成功"));

        } else if ("/reviewer/remove".equals(pathInfo)) {
            // 移除审稿人
            Integer userId = parseInt(req.getParameter("userId"));
            String reason = req.getParameter("reason");
            eicService.removeReviewer(userId, reason);
            writeJson(resp, Result.success("审稿人已移除"));

        } else if ("/retract".equals(pathInfo)) {
            // 撤稿
            EicDecisionDTO dto = objectMapper.readValue(req.getInputStream(), EicDecisionDTO.class);
            eicService.retractManuscript(dto);
            writeJson(resp, Result.success("稿件已撤回"));

        } else if ("/rescind-decision".equals(pathInfo)) {
            // 撤销决定
            Integer manuscriptId = parseInt(req.getParameter("manuscriptId"));
            String newStatus = req.getParameter("newStatus");
            String reason = req.getParameter("reason");
            eicService.rescindDecision(manuscriptId, newStatus, reason);
            writeJson(resp, Result.success("决策已撤销"));

        } else {
            resp.setStatus(404);
            writeJson(resp, Result.error("POST Endpoint not found: " + pathInfo));
        }
    }

    /**
     * 处理报表导出 (返回二进制流)
     */
    private void handleExportReport(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String startDate = req.getParameter("startDate");
        String endDate = req.getParameter("endDate");

        if (startDate == null || endDate == null) {
            writeJson(resp, Result.error("startDate and endDate are required"));
            return;
        }

        byte[] reportBytes = eicService.exportReport(startDate, endDate);

        // 设置响应头，覆盖默认的 application/json
        resp.setContentType("application/vnd.ms-excel");
        resp.setHeader("Content-Disposition",
                "attachment;filename=manuscript_report_" + startDate + "_to_" + endDate + ".xlsx");

        // 写入响应流
        ServletOutputStream out = resp.getOutputStream();
        out.write(reportBytes);
        out.flush();
    }

    /**
     * 处理获取稿件详情 (解析路径参数)
     */
    private void handleGetManuscriptDetails(HttpServletResponse resp, String pathInfo) throws IOException {
        // 提取 ID: /manuscript/{id}/details
        try {
            String[] parts = pathInfo.split("/");
            // parts[0] is empty, parts[1] is manuscript, parts[2] is id, parts[3] is details
            if (parts.length >= 3) {
                Integer id = Integer.parseInt(parts[2]);
                ManuscriptDetailDTO details = eicService.getManuscriptDetails(id);
                writeJson(resp, Result.success(details));
            } else {
                writeJson(resp, Result.error("Invalid path format"));
            }
        } catch (NumberFormatException e) {
            writeJson(resp, Result.error("Invalid Manuscript ID"));
        }
    }

    /**
     * 统一鉴权逻辑
     */
    private boolean authenticate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            return true;
        }

        String token = req.getHeader("Authorization");
        if (token != null && !token.isEmpty()) {
            try {
                Claims claims = JwtUtil.parseToken(token);
                if (claims != null) {
                    Integer userId = (Integer) claims.get("userId");
                    UserContext.setUserId(userId);
                    return true;
                }
            } catch (Exception e) {
                // Token 解析失败
            }
        }

        resp.setStatus(401);
        writeJson(resp, Result.error("401 Unauthorized: Please Login"));
        return false;
    }

    /**
     * 辅助方法：输出 JSON
     */
    private void writeJson(HttpServletResponse resp, Object data) throws IOException {
        // 如果 Content-Type 已经被修改（例如导出），则不再写入 JSON
        if (!resp.getContentType().startsWith("application/json")) {
            return;
        }
        PrintWriter out = resp.getWriter();
        out.print(objectMapper.writeValueAsString(data));
        out.flush();
    }

    /**
     * 辅助方法：安全解析 Integer
     */
    private Integer parseInt(String val) {
        try {
            return val != null ? Integer.parseInt(val) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}