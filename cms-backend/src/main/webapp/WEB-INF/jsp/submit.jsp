<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>论文发表 - 国际人工智能研究</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        /* 基础样式 */
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            line-height: 1.6;
            color: #333;
            background-color: #f8f9fa;
        }

        .container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 0 20px;
        }

        /* 导航栏样式 */
        .navbar {
            background: linear-gradient(135deg, #2c3e50, #34495e);
            color: white;
            padding: 1rem 0;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            position: sticky;
            top: 0;
            z-index: 1000;
        }

        .nav-container {
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .logo {
            font-size: 1.8rem;
            font-weight: bold;
            color: white;
            text-decoration: none;
        }

        .logo span {
            color: #3498db;
        }

        .nav-links {
            display: flex;
            list-style: none;
            gap: 2rem;
        }

        .nav-links a {
            color: white;
            text-decoration: none;
            font-weight: 500;
            transition: color 0.3s ease;
            padding: 0.5rem 1rem;
            border-radius: 4px;
        }

        .nav-links a:hover, .nav-links a.active {
            background-color: rgba(255,255,255,0.1);
            color: #3498db;
        }

        /* 页面标题 */
        .page-header {
            background: linear-gradient(135deg, #3498db, #2980b9);
            color: white;
            padding: 4rem 0;
            text-align: center;
            margin-bottom: 3rem;
        }

        .page-title {
            font-size: 2.5rem;
            margin-bottom: 1rem;
            font-weight: 300;
        }

        .page-subtitle {
            font-size: 1.2rem;
            opacity: 0.9;
            font-weight: 300;
        }

        /* 主要内容区域 */
        .main-content {
            display: flex;
            gap: 2rem;
            margin-bottom: 3rem;
        }

        /* 侧边栏样式 */
        .sidebar {
            flex: 0 0 300px;
            background: white;
            border-radius: 10px;
            box-shadow: 0 2px 20px rgba(0,0,0,0.1);
            padding: 2rem;
            height: fit-content;
            position: sticky;
            top: 100px;
        }

        .sidebar-title {
            color: #2c3e50;
            font-size: 1.5rem;
            margin-bottom: 1.5rem;
            padding-bottom: 0.5rem;
            border-bottom: 3px solid #3498db;
        }

        .menu-items {
            list-style: none;
        }

        .menu-item {
            margin-bottom: 1rem;
        }

        .menu-item a {
            display: flex;
            align-items: center;
            padding: 1rem;
            background: #f8f9fa;
            border-radius: 8px;
            text-decoration: none;
            color: #2c3e50;
            transition: all 0.3s ease;
            border-left: 4px solid transparent;
        }

        .menu-item a:hover, .menu-item a.active {
            background: #e3f2fd;
            border-left-color: #3498db;
            color: #3498db;
        }

        .menu-item i {
            margin-right: 1rem;
            font-size: 1.2rem;
            width: 20px;
            text-align: center;
        }

        /* 内容区域样式 */
        .content {
            flex: 1;
        }

        .section {
            background: white;
            margin-bottom: 2rem;
            padding: 2.5rem;
            border-radius: 10px;
            box-shadow: 0 2px 20px rgba(0,0,0,0.1);
        }

        .section-title {
            color: #2c3e50;
            font-size: 1.8rem;
            margin-bottom: 1.5rem;
            padding-bottom: 0.5rem;
            border-bottom: 3px solid #3498db;
            display: inline-block;
        }

        /* 投稿选项样式 */
        .submit-options {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 2rem;
            margin-top: 2rem;
        }

        .option-card {
            background: #f8f9fa;
            border-radius: 10px;
            padding: 2rem;
            text-align: center;
            transition: transform 0.3s ease, box-shadow 0.3s ease;
            border-left: 4px solid #3498db;
        }

        .option-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 5px 25px rgba(0,0,0,0.15);
        }

        .option-icon {
            width: 80px;
            height: 80px;
            background: #3498db;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 1.5rem;
            color: white;
            font-size: 2rem;
        }

        .option-title {
            color: #2c3e50;
            font-size: 1.5rem;
            margin-bottom: 1rem;
        }

        .option-description {
            color: #7f8c8d;
            margin-bottom: 1.5rem;
        }

        .btn {
            display: inline-block;
            background: #3498db;
            color: white;
            padding: 0.8rem 1.5rem;
            border-radius: 5px;
            text-decoration: none;
            font-weight: 500;
            transition: background 0.3s ease;
        }

        .btn:hover {
            background: #2980b9;
        }

        /* 作者指南样式 */
        .guide-steps {
            display: flex;
            flex-direction: column;
            gap: 2rem;
            margin-top: 2rem;
        }

        .step {
            display: flex;
            align-items: flex-start;
            gap: 1.5rem;
        }

        .step-number {
            width: 50px;
            height: 50px;
            background: #3498db;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-size: 1.5rem;
            font-weight: bold;
            flex-shrink: 0;
        }

        .step-content {
            flex: 1;
        }

        .step-title {
            color: #2c3e50;
            font-size: 1.3rem;
            margin-bottom: 0.5rem;
        }

        /* 征稿通知样式 - 修改为四点布局 */
        .cfp-points {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 2rem;
            margin-top: 2rem;
        }

        .cfp-point {
            background: #f8f9fa;
            border-radius: 10px;
            padding: 2rem;
            border-left: 4px solid #e74c3c;
            transition: transform 0.3s ease;
        }

        .cfp-point:hover {
            transform: translateY(-5px);
            box-shadow: 0 5px 15px rgba(0,0,0,0.1);
        }

        .cfp-point-header {
            display: flex;
            align-items: center;
            margin-bottom: 1rem;
        }

        .cfp-point-icon {
            width: 60px;
            height: 60px;
            background: #e74c3c;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-size: 1.5rem;
            margin-right: 1rem;
        }

        .cfp-point-title {
            color: #2c3e50;
            font-size: 1.3rem;
            margin: 0;
        }

        .cfp-point-content {
            color: #7f8c8d;
        }

        .cfp-point-content ul {
            margin-top: 1rem;
            padding-left: 1.5rem;
        }

        .cfp-point-content li {
            margin-bottom: 0.5rem;
        }

        /* 登录提示样式 */
        .login-prompt {
            background: #fff3cd;
            border: 1px solid #ffeaa7;
            border-radius: 8px;
            padding: 1.5rem;
            margin-top: 2rem;
            display: flex;
            align-items: center;
            gap: 1rem;
        }

        .login-prompt i {
            color: #f39c12;
            font-size: 1.5rem;
        }

        /* 响应式设计 */
        @media (max-width: 768px) {
            .nav-container {
                flex-direction: column;
                gap: 1rem;
            }

            .nav-links {
                flex-wrap: wrap;
                justify-content: center;
                gap: 1rem;
            }

            .main-content {
                flex-direction: column;
            }

            .sidebar {
                position: static;
                margin-bottom: 2rem;
            }

            .submit-options, .cfp-points {
                grid-template-columns: 1fr;
            }

            .step {
                flex-direction: column;
                text-align: center;
            }
        }
    </style>
</head>
<body>
<!-- 导航栏 -->
<nav class="navbar">
    <div class="container">
        <div class="nav-container">
            <a href="index" class="logo">国际<span>人工智能</span>研究</a>
            <ul class="nav-links">
                <li><a href="index">首页</a></li>
                <li><a href="about">关于期刊</a></li>
                <li><a href="submit" class="active">论文发表</a></li>
                <li><a href="articles">文章与专刊</a></li>
                <li><a href="guide">用户指南</a></li>
                <li><a href="http://localhost:5173/login">登录/注册</a></li>
            </ul>
        </div>
    </div>
</nav>

<!-- 页面标题 -->
<header class="page-header">
    <div class="container">
        <h1 class="page-title">论文发表</h1>
        <p class="page-subtitle">提交您的论文稿件，参与国际人工智能研究交流</p>
    </div>
</header>

<div class="container">
    <div class="main-content">
        <!-- 侧边栏菜单 -->
        <aside class="sidebar">
            <h2 class="sidebar-title">论文发表</h2>
            <ul class="menu-items">
                <li class="menu-item">
                    <a href="#submit" class="active">
                        <i class="fas fa-paper-plane"></i>
                        <span>Submit your article</span>
                    </a>
                </li>
                <li class="menu-item">
                    <a href="#guide">
                        <i class="fas fa-book"></i>
                        <span>Guide for authors</span>
                    </a>
                </li>
                <li class="menu-item">
                    <a href="#cfp">
                        <i class="fas fa-bullhorn"></i>
                        <span>Call for papers</span>
                    </a>
                </li>
            </ul>
        </aside>

        <!-- 主要内容区域 -->
        <div class="content">
            <!-- Submit your article 部分 -->
            <section id="submit" class="section">
                <h2 class="section-title">Submit your article</h2>
                <p>通过我们的在线投稿系统提交您的论文稿件。请确保您的稿件符合期刊的格式要求和范围。</p>

                <div class="submit-options">
                    <div class="option-card">
                        <div class="option-icon">
                            <i class="fas fa-file-upload"></i>
                        </div>
                        <h3 class="option-title">新稿件提交</h3>
                        <p class="option-description">首次向本期刊提交论文稿件</p>
                        <a href="http://localhost:5173/login" class="btn">开始投稿</a>
                    </div>

                    <div class="option-card">
                        <div class="option-icon">
                            <i class="fas fa-edit"></i>
                        </div>
                        <h3 class="option-title">修改稿提交</h3>
                        <p class="option-description">根据审稿意见修改后重新提交</p>
                        <a href="http://localhost:5173/login" class="btn">提交修改稿</a>
                    </div>

                    <div class="option-card">
                        <div class="option-icon">
                            <i class="fas fa-tasks"></i>
                        </div>
                        <h3 class="option-title">稿件状态查询</h3>
                        <p class="option-description">查看已投稿件的审稿进度</p>
                        <a href="http://localhost:5173/login" class="btn">查询状态</a>
                    </div>
                </div>

                <div class="login-prompt">
                    <i class="fas fa-info-circle"></i>
                    <div>
                        <p><strong>请注意：</strong>您需要登录账户才能提交论文。如果您还没有账户，请先<a href="http://localhost:5173/register">注册</a>。</p>
                    </div>
                </div>
            </section>

            <!-- Guide for authors 部分 -->
            <section id="guide" class="section">
                <h2 class="section-title">Guide for authors</h2>
                <p>为确保您的稿件顺利通过评审，请仔细阅读以下作者指南。</p>

                <div class="guide-steps">
                    <div class="step">
                        <div class="step-number">1</div>
                        <div class="step-content">
                            <h3 class="step-title">准备稿件</h3>
                            <p>确保您的稿件符合期刊的格式要求，包括标题、摘要、关键词、正文、参考文献等部分的格式规范。</p>
                            <p><strong>重要提示：</strong>稿件应为原创作品，未在其他地方发表过。</p>
                        </div>
                    </div>

                    <div class="step">
                        <div class="step-number">2</div>
                        <div class="step-content">
                            <h3 class="step-title">选择投稿类型</h3>
                            <p>根据您的研究内容选择合适的投稿类型：</p>
                            <ul>
                                <li><strong>研究论文：</strong>完整的原创研究成果</li>
                                <li><strong>综述文章：</strong>对某一领域的全面综述</li>
                                <li><strong>短篇通讯：</strong>重要的初步发现或技术说明</li>
                            </ul>
                        </div>
                    </div>

                    <div class="step">
                        <div class="step-number">3</div>
                        <div class="step-content">
                            <h3 class="step-title">提交稿件</h3>
                            <p>通过在线系统提交您的稿件，包括：</p>
                            <ul>
                                <li>稿件正文（Word或LaTeX格式）</li>
                                <li>所有图表文件</li>
                                <li>Cover Letter（说明稿件的重要性和创新点）</li>
                                <li>推荐审稿人（可选）</li>
                            </ul>
                        </div>
                    </div>

                    <div class="step">
                        <div class="step-number">4</div>
                        <div class="step-content">
                            <h3 class="step-title">审稿流程</h3>
                            <p>您的稿件将经过以下审稿流程：</p>
                            <ul>
                                <li><strong>形式审查：</strong>检查稿件格式是否符合要求</li>
                                <li><strong>学术初审：</strong>主编评估稿件的学术价值</li>
                                <li><strong>同行评审：</strong>2-3位领域专家进行双盲评审</li>
                                <li><strong>最终决定：</strong>主编根据审稿意见做出决定</li>
                            </ul>
                        </div>
                    </div>
                </div>
            </section>

            <!-- Call for papers 部分 - 四点布局优化 -->
            <section id="cfp" class="section">
                <h2 class="section-title">征稿通知 (Call for Papers)</h2>
                <p>欢迎投稿至以下特刊和专栏，共同推动人工智能领域的前沿研究。</p>

                <div class="cfp-points">
                    <c:choose>
                        <c:when test="${not empty callForPapers}">
                            <c:forEach var="cfp" items="${callForPapers}" varStatus="status">
                                <div class="cfp-point">
                                    <div class="cfp-point-header">
                                        <div class="cfp-point-icon">
                                            <i class="fas fa-bullhorn"></i>
                                        </div>
                                        <h3 class="cfp-point-title">
                                            <c:choose>
                                                <c:when test="${not empty cfp.title}">${cfp.title}</c:when>
                                                <c:otherwise>征稿通知 ${status.index + 1}</c:otherwise>
                                            </c:choose>
                                        </h3>
                                    </div>
                                    <div class="cfp-point-content">
                                        <c:if test="${not empty cfp.publishDate}">
                                            <p><strong>发布时间:</strong> ${cfp.publishDate}</p>
                                        </c:if>
                                        <c:choose>
                                            <c:when test="${not empty cfp.content}">
                                                <p>${cfp.content}</p>
                                            </c:when>
                                            <c:otherwise>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <p>暂无任何信息</p>
                        </c:otherwise>
                    </c:choose>
                </div>
            </section>
        </div>
    </div>
</div>

<!-- 页脚 -->
<footer style="background: #2c3e50; color: white; text-align: center; padding: 2rem; margin-top: 3rem;">
    <p>&copy; 2024 国际人工智能研究期刊. 保留所有权利.</p>
    <p>联系方式: contact@iair-journal.org | 电话: +86-10-62345678</p>
</footer>

<script>
    // 菜单项点击效果
    document.addEventListener('DOMContentLoaded', function() {
        const menuItems = document.querySelectorAll('.menu-item a');

        menuItems.forEach(item => {
            item.addEventListener('click', function(e) {
                // 移除所有active类
                menuItems.forEach(i => i.classList.remove('active'));
                // 添加当前active类
                this.classList.add('active');

                // 平滑滚动到对应部分
                const targetId = this.getAttribute('href');
                const targetSection = document.querySelector(targetId);
                if (targetSection) {
                    e.preventDefault();
                    window.scrollTo({
                        top: targetSection.offsetTop - 100,
                        behavior: 'smooth'
                    });
                }
            });
        });

        // 页面加载时根据URL哈希值激活对应菜单项
        const hash = window.location.hash;
        if (hash) {
            const correspondingMenuItem = document.querySelector(`.menu-item a[href="${hash}"]`);
            if (correspondingMenuItem) {
                menuItems.forEach(i => i.classList.remove('active'));
                correspondingMenuItem.classList.add('active');
            }
        }
    });
</script>
</body>
</html>