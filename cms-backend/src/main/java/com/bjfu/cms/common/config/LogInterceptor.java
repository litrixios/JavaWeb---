package com.bjfu.cms.common.config;

import com.bjfu.cms.service.LogService;
import com.bjfu.cms.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LogInterceptor implements HandlerInterceptor {

    @Autowired
    private LogService logService;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        // 在请求完成后记录日志
        if (response.getStatus() == 200) {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                try {
                    Claims claims = JwtUtil.parseToken(token);
                    Integer operatorId = (Integer) claims.get("userId");
                    String operationType = getOperationType(request);
                    String description = getOperationDescription(request);

                    logService.addLog(operatorId, operationType, null, description);
                } catch (Exception e) {
                    // 记录日志失败不影响主要业务
                }
            }
        }
    }

    private String getOperationType(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if (path.contains("/users") && "GET".equals(method)) return "UserQuery";
        if (path.contains("/users") && "POST".equals(method)) return "UserCreate";
        if (path.contains("/users") && "PUT".equals(method)) return "UserUpdate";
        if (path.contains("/users") && "DELETE".equals(method)) return "UserDelete";
        if (path.contains("/logs") && "DELETE".equals(method)) return "LogClear";

        return "System";
    }

    private String getOperationDescription(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if (path.contains("/users") && "GET".equals(method)) return "查询用户列表";
        if (path.contains("/users") && "POST".equals(method)) return "创建新用户";
        if (path.contains("/users") && "PUT".equals(method)) return "更新用户信息";
        if (path.contains("/users") && "DELETE".equals(method)) return "删除用户";
        if (path.contains("/logs") && "DELETE".equals(method)) return "清空系统日志";

        return "系统操作";
    }
}