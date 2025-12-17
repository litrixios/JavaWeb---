package com.bjfu.cms.common.config;

import com.bjfu.cms.common.utils.JwtUtil;
import com.bjfu.cms.common.utils.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest; // SpringBoot 3用jakarta，2用javax
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 如果是 OPTIONS 请求（前端预检），直接放行
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 2. 获取 Header 中的 Token
        String token = request.getHeader("Authorization");

        // 3. 校验 Token
        if (token != null && !token.isEmpty()) {
            Claims claims = JwtUtil.parseToken(token); // 解密过程
            if (claims != null) {
                // 校验通过，将 userId 存入 ThreadLocal，方便后续 Controller 使用
                Integer userId = (Integer) claims.get("userId");
                UserContext.setUserId(userId);
                return true; // 放行
            }
        }

        // 4. 校验失败，设置响应状态码 401
        response.setStatus(401);
        response.getWriter().write("{\"code\": 401, \"msg\": \"Unauthorized: Please Login\"}");
        return false; // 拦截
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束，清理线程变量，防止内存泄漏
        UserContext.remove();
    }
}