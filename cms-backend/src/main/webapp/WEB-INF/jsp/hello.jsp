<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>JSP 测试页面</title>
    <style>
        body {
            font-family: "Microsoft YaHei", sans-serif;
            text-align: center;
            margin-top: 50px;
        }
        h1 {
            color: #007bff;
        }
        .box {
            border: 2px dashed #ccc;
            padding: 20px;
            display: inline-block;
            background-color: #f9f9f9;
        }
    </style>
</head>
<body>

<div class="box">
    <h1>你好，Spring Boot + JSP！</h1>

    <p>后端传来的名字是：<strong style="color: red; font-size: 24px;">${name}</strong></p>

    <p>当前时间：<%= new java.util.Date() %></p>
    <a href="http://localhost:5176/manuscript/list">点击跳转到 Vue 前端</a></div>

</body>
</html>