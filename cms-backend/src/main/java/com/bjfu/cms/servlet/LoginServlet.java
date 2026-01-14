package com.bjfu.cms.servlet;

import com.bjfu.cms.common.result.Result;
import com.bjfu.cms.common.utils.JwtUtil;
import com.bjfu.cms.entity.User;
import com.bjfu.cms.entity.dto.LoginDTO;
import com.bjfu.cms.service.UserService;
import com.bjfu.cms.service.impl.UserServiceImpl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import jakarta.servlet.ServletContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;


@WebServlet(name = "LoginServlet", value = "/api/auth/login")
public class LoginServlet extends HttpServlet {

    private UserService userService;

    @Override
    public void init() throws ServletException {
        // 初始化服务层
        userService = new UserServiceImpl();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

//        // 设置响应头
//        response.setContentType("application/json;charset=utf-8");
//        response.setCharacterEncoding("UTF-8");
//
//        // 设置CORS头部
//        setCorsHeaders(response);
//
//        PrintWriter out = response.getWriter();
//
//        try {
//            // 1. 读取请求体中的JSON数据
//            StringBuilder jsonBuilder = new StringBuilder();
//            BufferedReader reader = request.getReader();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                jsonBuilder.append(line);
//            }
//
//            String json = jsonBuilder.toString();
//            System.out.println("接收到登录请求: " + json);
//
//            // 2. 解析JSON数据到LoginDTO
//            LoginDTO loginDTO = JSON.parseObject(json, LoginDTO.class);
//
//            if (loginDTO == null ||
//                    loginDTO.getUsername() == null || loginDTO.getUsername().trim().isEmpty() ||
//                    loginDTO.getPassword() == null || loginDTO.getPassword().trim().isEmpty() ||
//                    loginDTO.getRole() == null || loginDTO.getRole().trim().isEmpty()) {
//
//                Result<Map<String, Object>> result = Result.error( "用户名、密码和身份不能为空");
//                out.print(JSON.toJSONString(result));
//                return;
//            }
//
//            // 3. 查询用户
//           // User user = userService.findByUsername(loginDTO.getUsername());
//            User user=null;
//            // 4. 校验用户是否存在
//            if (user == null) {
//                System.out.println("用户不存在: " + loginDTO.getUsername());
//                Result<Map<String, Object>> result = Result.error("用户不存在");
//                out.print(JSON.toJSONString(result));
//                return;
//            }
//
//            // 5. 校验身份
//            if (!user.getRole().equals(loginDTO.getRole())) {
//                System.out.println("身份错误: 用户角色=" + user.getRole() + ", 请求角色=" + loginDTO.getRole());
//                Result<Map<String, Object>> result = Result.error("身份错误");
//                out.print(JSON.toJSONString(result));
//                return;
//            }
//
//            // 6. 校验密码 (这里保持明文比对，与Spring Boot版本一致)
//            if (!user.getPassword().equals(loginDTO.getPassword())) {
//                System.out.println("密码错误");
//                Result<Map<String, Object>> result = Result.error("密码错误");
//                out.print(JSON.toJSONString(result));
//                return;
//            }
//
//            // 7. 检查状态
//            if (user.getStatus() == 2) {
//                System.out.println("账号被禁用");
//                Result<Map<String, Object>> result = Result.error("账号已被禁用");
//                out.print(JSON.toJSONString(result));
//                return;
//            }
//
//            // 8. 生成 Token
//            String token = JwtUtil.generateToken(user.getUserId(), user.getUsername(), user.getRole());
//
//            // 9. 返回结果 (Token + 用户基本信息)
//            // 创建用户信息Map，排除敏感信息
//            Map<String, Object> userInfo = new HashMap<>();
//            userInfo.put("userId", user.getUserId());
//            userInfo.put("username", user.getUsername());
//            userInfo.put("email", user.getEmail());
//          //  userInfo.put("realName", user.getRealName());
//            userInfo.put("role", user.getRole());
//            userInfo.put("status", user.getStatus());
//
//            Map<String, Object> data = new HashMap<>();
//            data.put("token", token);
//            data.put("user", userInfo);
//
//            System.out.println("登录成功，返回数据: " + data);
//
//            // 10. 更新最后登录时间
//            //userService.updateLastLoginTime(user.getUserId());
//
//            Result<Map<String, Object>> result = Result.success(data);
//            out.print(JSON.toJSONString(result));
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            Result<Map<String, Object>> result = Result.error("系统错误：" + e.getMessage());
//            out.print(JSON.toJSONString(result));
//        } finally {
//            out.flush();
//            out.close();
//        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=utf-8");
        response.setCharacterEncoding("UTF-8");
        setCorsHeaders(response);

        PrintWriter out = response.getWriter();
        Result<Map<String, Object>> result = Result.error( "不支持GET请求，请使用POST方法");
        out.print(JSON.toJSONString(result));
        out.flush();
        out.close();
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 处理预检请求
        setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:8080");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers",
                "Content-Type, Authorization, X-Requested-With, Accept, Origin");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Max-Age", "3600");
    }
}