package com.bjfu.cms.servlet;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bjfu.cms.common.result.Result;
import com.bjfu.cms.common.utils.JwtUtil;
import com.bjfu.cms.common.utils.UserContext;
import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.Review;
import com.bjfu.cms.entity.dto.ReviewInvitationResponseDTO;
import com.bjfu.cms.entity.dto.ReviewSubmitDTO;
import com.bjfu.cms.service.ReviewerService;
import com.bjfu.cms.service.SftpService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.List;

/**
 * 审稿人功能模块 Servlet 改写版
 * 替换原 ReviewerController
 */
@WebServlet(name = "ReviewerServlet", urlPatterns = "/api/reviewer/*")
public class ReviewerServlet extends HttpServlet {

    private ReviewerService reviewerService;
    private SftpService sftpService;

    @Override
    public void init() throws ServletException {
        // 关键步骤：从 Spring Web 上下文中手动获取 Service Bean
        // 这样可以复用原有的 Service 业务逻辑（包括事务、数据库操作等）
        var ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        this.reviewerService = ctx.getBean(ReviewerService.class);
        this.sftpService = ctx.getBean(SftpService.class);
    }

    /**
     * 重写 service 方法以统一处理 鉴权(Auth)、跨域(CORS) 和 上下文清理
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1. 处理跨域 (CORS)
        String origin = req.getHeader("Origin");
        if (origin != null) {
            resp.setHeader("Access-Control-Allow-Origin", origin);
            resp.setHeader("Access-Control-Allow-Credentials", "true");
            resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            resp.setHeader("Access-Control-Allow-Headers", "*");
        }

        // 处理预检请求
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            resp.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // 设置响应类型
        if (!req.getRequestURI().contains("/download/")) {
            resp.setContentType("application/json;charset=UTF-8");
        }
        resp.setCharacterEncoding("UTF-8");

        // 2. 鉴权逻辑 (模拟 JwtInterceptor)
        // 注意：Servlet 不会被 Spring 的 Interceptor 拦截，必须手动处理
        try {
            if (!handleAuthentication(req, resp)) {
                return; // 鉴权失败，已写入 401 响应
            }

            // 3. 路由分发
            super.service(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = resp.getWriter()) {
                out.print(JSON.toJSONString(Result.error("服务器内部错误: " + e.getMessage())));
            }
        } finally {
            // 4. 清理 ThreadLocal，防止内存泄漏 (非常重要)
            UserContext.remove();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo(); // 例如: /my-reviews
        PrintWriter out = resp.getWriter();

        if (pathInfo == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            if ("/my-reviews".equals(pathInfo)) {
                // 获取我的审稿任务列表
                List<Review> reviews = reviewerService.getMyReviews();
                out.print(JSON.toJSONString(Result.success(reviews)));

            } else if ("/pending-invitations".equals(pathInfo)) {
                // 查看待处理的审稿邀请
                List<Review> reviews = reviewerService.getPendingInvitations();
                out.print(JSON.toJSONString(Result.success(reviews)));

            } else if (pathInfo.startsWith("/invitation/") && pathInfo.endsWith("/manuscript")) {
                // 查看邀请的稿件详情: /invitation/{reviewId}/manuscript
                // 解析 reviewId
                String[] parts = pathInfo.split("/"); // ["", "invitation", "123", "manuscript"]
                if (parts.length >= 4) {
                    Integer reviewId = Integer.parseInt(parts[2]);
                    Manuscript manuscript = reviewerService.getManuscriptByReviewId(reviewId);
                    out.print(JSON.toJSONString(Result.success(manuscript)));
                } else {
                    out.print(JSON.toJSONString(Result.error("参数错误")));
                }

            } else if (pathInfo.startsWith("/manuscript/download/")) {
                // 下载/预览匿名稿件文件: /manuscript/download/{reviewId}
                // 注意：这里不能使用 out (PrintWriter)，因为下载需要二进制流
                String[] parts = pathInfo.split("/");
                if (parts.length >= 4) {
                    Integer reviewId = Integer.parseInt(parts[3]);
                    handleDownload(reviewId, resp);
                }

            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(JSON.toJSONString(Result.error("接口不存在: " + pathInfo)));
            }
        } catch (Exception e) {
            out.print(JSON.toJSONString(Result.error("请求处理失败: " + e.getMessage())));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        PrintWriter out = resp.getWriter();

        if ("/invitation/respond".equals(pathInfo)) {
            // 接受/拒绝审稿邀请
            String body = readBody(req);
            ReviewInvitationResponseDTO dto = JSON.parseObject(body, ReviewInvitationResponseDTO.class);

            if (dto.isAccepted()) {
                reviewerService.acceptInvitation(dto.getReviewId());
                String deadline = reviewerService.getDeadline(dto.getReviewId());
                out.print(JSON.toJSONString(Result.success("审稿任务已接受，截止日期：" + deadline)));
            } else {
                if (dto.getReason() == null || dto.getReason().trim().isEmpty()) {
                    out.print(JSON.toJSONString(Result.error("拒绝审稿必须填写理由")));
                    return;
                }
                reviewerService.rejectInvitation(dto.getReviewId(), dto.getReason());
                out.print(JSON.toJSONString(Result.success("已拒绝审稿邀请")));
            }

        } else if ("/submit-review".equals(pathInfo)) {
            // 提交审稿意见
            String body = readBody(req);
            ReviewSubmitDTO dto = JSON.parseObject(body, ReviewSubmitDTO.class);

            if (dto.getRecommendation() == null) {
                out.print(JSON.toJSONString(Result.error("请选择总体建议（接受/修改/拒稿）")));
                return;
            }
            reviewerService.submitReview(dto);
            out.print(JSON.toJSONString(Result.success("审稿意见提交成功")));

        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print(JSON.toJSONString(Result.error("接口不存在")));
        }
    }

    /**
     * 处理文件下载
     */
    private void handleDownload(Integer reviewId, HttpServletResponse response) throws IOException {
        try {
            // 1. 获取匿名文件路径
            String filePath = reviewerService.getAnonymousFilePath(reviewId);

            // 2. 获取文件名
            String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);

            // 3. 设置响应头
            response.reset();
            response.setContentType("application/pdf");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));

            // 4. 调用文件服务将文件写入 Response 输出流
            // 注意：不要再获取 Writer，否则会冲突
            sftpService.downloadToStream(filePath, response.getOutputStream());

        } catch (Exception e) {
            // 如果已开始写入流，可能无法通过 JSON 返回错误，只能记录日志
            e.printStackTrace();
            // 如果 response 还没提交，尝试返回错误
            if (!response.isCommitted()) {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().print(JSON.toJSONString(Result.error("文件下载失败：" + e.getMessage())));
            }
        }
    }

    /**
     * 简单的鉴权逻辑，复用 JwtUtil
     */
    private boolean handleAuthentication(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String token = request.getHeader("Authorization");

        if (token != null && !token.isEmpty()) {
            Claims claims = JwtUtil.parseToken(token);
            if (claims != null) {
                Integer userId = (Integer) claims.get("userId");
                String role = (String) claims.get("role"); // 如果需要校验角色

                // 设置上下文，供 Service 层使用
                UserContext.setUserId(userId);
                UserContext.setUserRole(role);
                return true;
            }
        }

        // 鉴权失败
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{\"code\": 401, \"msg\": \"Unauthorized: Please Login\"}");
        return false;
    }

    private String readBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }
}