<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>关于期刊 - 国际人工智能研究</title>
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

        /* 内容区块样式 */
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

        /* 论文主旨与投稿范围样式 */
        .aims-scope-content {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 2rem;
        }

        .scope-item {
            margin-bottom: 1.5rem;
        }

        .scope-item h3 {
            color: #3498db;
            margin-bottom: 0.5rem;
            display: flex;
            align-items: center;
        }

        .scope-item h3 i {
            margin-right: 10px;
        }

        /* 编委成员样式 */
        .editorial-board {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 2rem;
            margin-top: 2rem;
        }

        .editor-card {
            background: #f8f9fa;
            border-radius: 10px;
            padding: 1.5rem;
            transition: transform 0.3s ease, box-shadow 0.3s ease;
            border-left: 4px solid #3498db;
            text-align: center;
        }

        .editor-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 5px 25px rgba(0,0,0,0.15);
        }

        .editor-photo {
            width: 100px;
            height: 100px;
            border-radius: 50%;
            object-fit: cover;
            border: 3px solid #3498db;
            margin: 0 auto 1rem;
            background: #e0e0e0;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 2.5rem;
            color: #7f8c8d;
        }

        .editor-info h3 {
            color: #2c3e50;
            margin-bottom: 0.5rem;
        }

        .editor-role {
            color: #3498db;
            font-weight: 600;
            margin-bottom: 0.5rem;
        }

        .editor-affiliation {
            color: #7f8c8d;
            font-size: 0.9rem;
        }

        .editor-bio {
            color: #666;
            line-height: 1.6;
            margin-top: 1rem;
            font-size: 0.9rem;
            text-align: left;
        }

        /* 期刊信息样式 */
        .journal-metrics {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 1.5rem;
            margin-top: 1.5rem;
        }

        .metric-card {
            background: linear-gradient(135deg, #3498db, #2980b9);
            color: white;
            padding: 1.5rem;
            border-radius: 8px;
            text-align: center;
        }

        .metric-value {
            font-size: 2.5rem;
            font-weight: bold;
            margin-bottom: 0.5rem;
        }

        .metric-label {
            font-size: 0.9rem;
            opacity: 0.9;
        }

        .timeline {
            margin-top: 2rem;
            position: relative;
        }

        .timeline:before {
            content: '';
            position: absolute;
            left: 50%;
            top: 0;
            bottom: 0;
            width: 2px;
            background: #3498db;
            transform: translateX(-50%);
        }

        .timeline-item {
            display: flex;
            margin-bottom: 2rem;
            position: relative;
        }

        .timeline-item:nth-child(odd) {
            flex-direction: row;
        }

        .timeline-item:nth-child(even) {
            flex-direction: row-reverse;
        }

        .timeline-content {
            flex: 1;
            padding: 1rem;
            background: #f8f9fa;
            border-radius: 8px;
            margin: 0 2rem;
        }

        .timeline-date {
            font-weight: bold;
            color: #3498db;
            margin-bottom: 0.5rem;
        }

        .timeline-dot {
            width: 20px;
            height: 20px;
            background: #3498db;
            border-radius: 50%;
            position: absolute;
            left: 50%;
            top: 50%;
            transform: translate(-50%, -50%);
            z-index: 1;
        }

        /* 新闻列表样式 */
        .news-list {
            list-style: none;
        }

        .news-item {
            padding: 1.5rem;
            border-bottom: 1px solid #eee;
            transition: background-color 0.3s ease;
            display: flex;
            align-items: center;
        }

        .news-item:hover {
            background-color: #f8f9fa;
        }

        .news-item:last-child {
            border-bottom: none;
        }

        .news-icon {
            width: 50px;
            height: 50px;
            background: #3498db;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-size: 1.5rem;
            margin-right: 1.5rem;
            flex-shrink: 0;
        }

        .news-content {
            flex: 1;
        }

        .news-title {
            color: #2c3e50;
            font-weight: 600;
            margin-bottom: 0.5rem;
        }

        .news-date {
            color: #7f8c8d;
            font-size: 0.9rem;
        }

        /* 政策与指南样式 */
        .policies-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 1.5rem;
            margin-top: 1.5rem;
        }

        .policy-card {
            background: #f8f9fa;
            border-radius: 8px;
            padding: 1.5rem;
            border-left: 4px solid #e74c3c;
            transition: transform 0.3s ease;
        }

        .policy-card:hover {
            transform: translateY(-3px);
        }

        .policy-title {
            color: #2c3e50;
            margin-bottom: 0.5rem;
            display: flex;
            align-items: center;
        }

        .policy-title i {
            margin-right: 10px;
            color: #e74c3c;
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

            .aims-scope-content {
                grid-template-columns: 1fr;
            }

            .timeline:before {
                left: 20px;
            }

            .timeline-item {
                flex-direction: row !important;
            }

            .timeline-content {
                margin-left: 3rem;
                margin-right: 0;
            }

            .timeline-dot {
                left: 20px;
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
                <li><a href="about" class="active">关于期刊</a></li>
                <li><a href="submit">论文发表</a></li>
                <li><a href="articles">文章与专刊</a></li>
                <li><a href="guide">用户指南</a></li>
                <li><a href="login">登录/注册</a></li>
            </ul>
        </div>
    </div>
</nav>

<!-- 页面标题 -->
<header class="page-header">
    <div class="container">
        <h1 class="page-title">关于期刊</h1>
        <p class="page-subtitle">International Artificial Intelligence Research - 推动人工智能前沿研究</p>
    </div>
</header>

<div class="container">
    <!-- 论文主旨与投稿范围 -->
    <section class="section">
        <h2 class="section-title">论文主旨与投稿范围 (Aims and Scope)</h2>
        <div class="aims-scope-content">
            <div>
                <div class="scope-item">
                    <h3><i class="fas fa-bullseye"></i> 期刊主旨</h3>
                    <p>《国际人工智能研究》(International Artificial Intelligence Research, IAIR)致力于发表人工智能领域的高质量原创研究成果，推动人工智能理论与应用的前沿发展。</p>
                </div>
                <div class="scope-item">
                    <h3><i class="fas fa-book-open"></i> 收录范围</h3>
                    <p>本期刊涵盖人工智能领域的广泛研究方向，包括但不限于：机器学习、深度学习、自然语言处理、计算机视觉、机器人学、知识表示与推理等。</p>
                </div>
            </div>
            <div>
                <div class="scope-item">
                    <h3><i class="fas fa-pencil-alt"></i> 投稿类型</h3>
                    <p>接收原创研究论文、综述文章、短篇通讯、技术报告等多种类型的投稿，为不同阶段的研究成果提供发表平台。</p>
                </div>
                <div class="scope-item">
                    <h3><i class="fas fa-users"></i> 目标读者</h3>
                    <p>面向人工智能领域的研究人员、工程师、学者以及相关行业从业者，促进学术交流与技术转化。</p>
                </div>
            </div>
        </div>
    </section>

    <!-- 编委介绍部分 -->
    <section class="section">
        <h2 class="section-title">编委介绍</h2>
        <p>我们的编委会由来自全球顶尖研究机构的知名专家组成，确保期刊的学术质量和国际影响力。</p>

        <div class="editorial-board">
            <c:choose>
                <c:when test="${not empty editorialBoard}">
                    <c:forEach var="editor" items="${editorialBoard}" varStatus="status">
                        <div class="editor-card">
                            <div class="editor-header">
                                <!-- 添加头像占位符 -->
                                <div class="editor-photo" style="background: #3498db; color: white; display: flex; align-items: center; justify-content: center; font-size: 1.5rem; font-weight: bold;">
                                        ${editor.FullName.charAt(0)}
                                </div>
                                <div class="editor-info">
                                    <h3>${editor.FullName}</h3>
                                    <div class="editor-role">
                                        <strong>${editor.Position}</strong> - ${editor.Introduction}
                                    </div>
                                </div>
                            </div>
                            <div class="editor-bio">
                                <p><strong>研究领域:</strong> ${editor.Section}</p>
                            </div>
                        </div>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <div style="text-align: center; padding: 20px; color: #666;">
                        <p>暂无编委信息</p>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </section>

    <!-- 期刊信息 -->
    <section class="section">
        <h2 class="section-title">期刊信息 (Journal Insights)</h2>

        <div class="journal-metrics">
            <div class="metric-card">
                <div class="metric-value">8.5</div>
                <div class="metric-label">影响因子 (2024)</div>
            </div>
            <div class="metric-card">
                <div class="metric-value">15</div>
                <div class="metric-label">天平均初审时间</div>
            </div>
            <div class="metric-card">
                <div class="metric-value">89%</div>
                <div class="metric-label">作者满意度</div>
            </div>
            <div class="metric-card">
                <div class="metric-value">1234-5678</div>
                <div class="metric-label">ISSN</div>
            </div>
        </div>

        <h3 style="color: #2c3e50; margin: 2rem 0 1rem 0;">发表时间线 (Publishing Timeline)</h3>
        <div class="timeline">
            <div class="timeline-item">
                <div class="timeline-content">
                    <div class="timeline-date">投稿提交</div>
                    <p>作者通过在线系统提交稿件，系统自动进行格式检查。</p>
                </div>
                <div class="timeline-dot"></div>
            </div>
            <div class="timeline-item">
                <div class="timeline-content">
                    <div class="timeline-date">初审阶段 (1-2周)</div>
                    <p>编辑部进行形式审查，主编进行学术初审。</p>
                </div>
                <div class="timeline-dot"></div>
            </div>
            <div class="timeline-item">
                <div class="timeline-content">
                    <div class="timeline-date">同行评审 (4-6周)</div>
                    <p>邀请2-3位审稿人进行双盲评审。</p>
                </div>
                <div class="timeline-dot"></div>
            </div>
            <div class="timeline-item">
                <div class="timeline-content">
                    <div class="timeline-date">修改与再审 (2-4周)</div>
                    <p>作者根据审稿意见修改稿件。</p>
                </div>
                <div class="timeline-dot"></div>
            </div>
            <div class="timeline-item">
                <div class="timeline-content">
                    <div class="timeline-date">最终决定</div>
                    <p>主编根据审稿意见做出最终决定。</p>
                </div>
                <div class="timeline-dot"></div>
            </div>
            <div class="timeline-item">
                <div class="timeline-content">
                    <div class="timeline-date">在线发表</div>
                    <p>接受稿件在线优先发表。</p>
                </div>
                <div class="timeline-dot"></div>
            </div>
        </div>
    </section>

    <!-- 新闻 -->
    <section class="section">
        <h2 class="section-title">最新新闻</h2>
        <ul class="news-list">
            <li class="news-item">
                <div class="news-icon">
                    <i class="fas fa-trophy"></i>
                </div>
                <div class="news-content">
                    <div class="news-title">期刊影响因子提升至8.5，创历史新高</div>
                    <div class="news-date">2024-12-01</div>
                </div>
            </li>
            <li class="news-item">
                <div class="news-icon">
                    <i class="fas fa-award"></i>
                </div>
                <div class="news-content">
                    <div class="news-title">期刊入选SCI核心数据库</div>
                    <div class="news-date">2024-11-25</div>
                </div>
            </li>
            <li class="news-item">
                <div class="news-icon">
                    <i class="fas fa-handshake"></i>
                </div>
                <div class="news-content">
                    <div class="news-title">与IEEE建立战略合作关系</div>
                    <div class="news-date">2024-11-15</div>
                </div>
            </li>
            <li class="news-item">
                <div class="news-icon">
                    <i class="fas fa-users"></i>
                </div>
                <div class="news-content">
                    <div class="news-title">2024年度优秀论文评选结果公布</div>
                    <div class="news-date">2024-11-05</div>
                </div>
            </li>
        </ul>
    </section>

    <!-- 政策与指南 -->
    <section class="section">
        <h2 class="section-title">政策与指南 (Policies and Guidelines)</h2>
        <p>投稿前请仔细阅读以下政策与指南，确保您的稿件符合期刊要求。</p>

        <div class="policies-grid">
            <div class="policy-card">
                <h3 class="policy-title"><i class="fas fa-user-check"></i> 作者伦理指南</h3>
                <p>确保研究诚信，避免学术不端行为，包括抄袭、数据造假等。</p>
            </div>
            <div class="policy-card">
                <h3 class="policy-title"><i class="fas fa-file-alt"></i> 稿件格式要求</h3>
                <p>详细的稿件格式模板和排版指南，确保稿件符合期刊标准。</p>
            </div>
            <div class="policy-card">
                <h3 class="policy-title"><i class="fas fa-eye"></i> 审稿流程说明</h3>
                <p>了解期刊的双盲评审流程和审稿标准。</p>
            </div>
            <div class="policy-card">
                <h3 class="policy-title"><i class="fas fa-balance-scale"></i> 版权政策</h3>
                <p>关于稿件版权转让和开放获取选项的详细说明。</p>
            </div>
            <div class="policy-card">
                <h3 class="policy-title"><i class="fas fa-database"></i> 数据共享政策</h3>
                <p>鼓励作者共享研究数据，促进科学研究的可重复性。</p>
            </div>
            <div class="policy-card">
                <h3 class="policy-title"><i class="fas fa-gavel"></i> 申诉流程</h3>
                <p>对审稿决定有异议时的申诉流程和联系方式。</p>
            </div>
        </div>
    </section>
</div>

<!-- 页脚 -->
<footer style="background: #2c3e50; color: white; text-align: center; padding: 2rem; margin-top: 3rem;">
    <p>&copy; 2024 国际人工智能研究期刊. 保留所有权利.</p>
    <p>联系方式: contact@iair-journal.org | 电话: +86-10-62345678</p>
</footer>

<script>
    // 简单的交互效果
    document.addEventListener('DOMContentLoaded', function() {
        // 为编委卡片添加点击效果
        const editorCards = document.querySelectorAll('.editor-card');
        editorCards.forEach(card => {
            card.addEventListener('click', function() {
                this.style.transform = 'scale(0.98)';
                setTimeout(() => {
                    this.style.transform = '';
                }, 150);
            });
        });

        // 为政策卡片添加悬停效果
        const policyCards = document.querySelectorAll('.policy-card');
        policyCards.forEach(card => {
            card.addEventListener('mouseenter', function() {
                this.style.boxShadow = '0 5px 15px rgba(0,0,0,0.1)';
            });

            card.addEventListener('mouseleave', function() {
                this.style.boxShadow = '';
            });
        });
    });
</script>
</body>
</html>