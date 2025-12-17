package com.bjfu.cms.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {

    // 密钥（随便写，但不要泄露给前端）
    private static final String SECRET_KEY = "BIG ASS";
    // 过期时间：24小时 (毫秒)
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24;

    /**
     * 生成 Token
     * @param userId 用户ID
     * @param role 用户角色 (Frontend 根据这个判断显示什么菜单)
     * @return 加密的 Token 字符串
     */
    public static String generateToken(Integer userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username) //以此作为主要标识
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    /**
     * 解析 Token 获取 Claims (包含 userId, role 等信息)
     */
    public static Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY) //与私章作比较
                    .parseClaimsJws(token)     // 拆开token
                    .getBody();                //拿出里面的数据
        } catch (Exception e) {
            return null; // 解析失败或过期
        }
    }
}