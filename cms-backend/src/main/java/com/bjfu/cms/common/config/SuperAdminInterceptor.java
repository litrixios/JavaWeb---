package com.bjfu.cms.common.config;

import com.bjfu.cms.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.web.servlet.HandlerInterceptor;

//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest; // SpringBoot 3用jakarta，2用javax
import jakarta.servlet.http.HttpServletResponse;
public class SuperAdminInterceptor implements HandlerInterceptor {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 检查是否为超级管理员
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                Claims claims = JwtUtil.parseToken(token);
                String role = (String) claims.get("role");
                if ("SuperAdmin".equals(role)) {
                    return true;
                }
            } catch (Exception e) {
                // Token解析失败
            }
        }

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write("{\"code\":403,\"message\":\"权限不足，需要超级管理员权限\"}");
        return false;
    }
}