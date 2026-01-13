package com.bjfu.cms.servlet;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bjfu.cms.common.result.Result;
import com.bjfu.cms.common.utils.JwtUtil;
import com.bjfu.cms.entity.User;
import com.bjfu.cms.entity.dto.LoginDTO;
import com.bjfu.cms.entity.dto.RegisterDTO;
import com.bjfu.cms.mapper.UserMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

    private SqlSessionFactory sqlSessionFactory;
    private UserMapper userMapper;

    @Override
    public void init() throws ServletException {
        try {
            String resource = "mybatis-config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new ServletException("初始化MyBatis失败", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        PrintWriter out = response.getWriter();

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            userMapper = sqlSession.getMapper(UserMapper.class);

            if ("/login".equals(pathInfo)) {
                handleLogin(request, response, out);
            } else if ("/register".equals(pathInfo)) {
                handleRegister(request, response, out);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(JSON.toJSONString(Result.error("接口不存在")));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JSON.toJSONString(Result.error("服务器内部错误")));
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response, PrintWriter out)
            throws IOException {

        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        LoginDTO loginDTO = JSON.parseObject(sb.toString(), LoginDTO.class);

        // 查询用户
        User user = userMapper.findByUsername(loginDTO.getUsername());

        if (user == null) {
            out.print(JSON.toJSONString(Result.error("用户不存在")));
            return;
        }

        if (!user.getRole().equals(loginDTO.getRole())) {
            out.print(JSON.toJSONString(Result.error("身份错误")));
            return;
        }

        if (!user.getPassword().equals(loginDTO.getPassword())) {
            out.print(JSON.toJSONString(Result.error("密码错误")));
            return;
        }

        if (user.getStatus() == 2) {
            out.print(JSON.toJSONString(Result.error("账号已被禁用")));
            return;
        }

        // 生成Token
        String token = JwtUtil.generateToken(user.getUserId(), user.getUsername(), user.getRole());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);

        out.print(JSON.toJSONString(Result.success(data)));
    }

    private void handleRegister(HttpServletRequest request, HttpServletResponse response, PrintWriter out)
            throws IOException {

        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        RegisterDTO registerDTO = JSON.parseObject(sb.toString(), RegisterDTO.class);

        try {
            // 检查用户名是否已存在
            User existingUser = userMapper.findByUsername(registerDTO.getUsername());
            if (existingUser != null) {
                out.print(JSON.toJSONString(Result.error("用户名已存在")));
                return;
            }

            // 检查邮箱是否已存在
            if (userMapper.findByEmail(registerDTO.getEmail()) != null) {
                out.print(JSON.toJSONString(Result.error("邮箱已被注册")));
                return;
            }

            // 创建新用户
            User newUser = new User();
            newUser.setUsername(registerDTO.getUsername());
            newUser.setPassword(registerDTO.getPassword());
            newUser.setEmail(registerDTO.getEmail());
            newUser.setFullName(registerDTO.getFullName());
            newUser.setRole(registerDTO.getRole());
            newUser.setAffiliation(registerDTO.getAffiliation());
            newUser.setResearchDirection(registerDTO.getResearchDirection());
            newUser.setRegisterTime(new Date());
            newUser.setStatus(1);

            // 插入用户
            userMapper.insertUser(newUser);

            // 设置默认权限
            // 这里需要根据您的权限实体类进行调整
            // userMapper.insertUserPermissions(permissions);

            // 生成Token
            String token = JwtUtil.generateToken(newUser.getUserId(), newUser.getUsername(), newUser.getRole());

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", newUser);

            out.print(JSON.toJSONString(Result.success(data)));

        } catch (Exception e) {
            out.print(JSON.toJSONString(Result.error("注册失败，请稍后重试")));
        }
    }
}