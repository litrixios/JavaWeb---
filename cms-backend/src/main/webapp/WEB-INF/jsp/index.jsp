<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>首页 - 国际人工智能研究</title>
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

    /* 编委介绍样式 */
    .editorial-board {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
      gap: 2rem;
      margin-top: 2rem;
    }

    .editor-card {
      background: #f8f9fa;
      border-radius: 10px;
      padding: 1.5rem;
      transition: transform 0.3s ease, box-shadow 0.3s ease;
      border-left: 4px solid #3498db;
    }

    .editor-card:hover {
      transform: translateY(-5px);
      box-shadow: 0 5px 25px rgba(0,0,0,0.15);
    }

    .editor-header {
      display: flex;
      align-items: center;
      margin-bottom: 1rem;
    }

    .editor-photo {
      width: 80px;
      height: 80px;
      border-radius: 50%;
      object-fit: cover;
      border: 3px solid #3498db;
      margin-right: 1rem;
    }

    .editor-info h3 {
      color: #2c3e50;
      margin-bottom: 0.5rem;
    }

    .editor-role {
      color: #3498db;
      font-weight: 600;
      font-size: 0.9rem;
    }

    .editor-bio {
      color: #666;
      line-height: 1.6;
    }

    /* 论文列表样式 */
    .paper-tabs {
      display: flex;
      gap: 1rem;
      margin-bottom: 2rem;
      flex-wrap: wrap;
    }

    .tab-btn {
      padding: 0.8rem 1.5rem;
      background: #ecf0f1;
      border: none;
      border-radius: 25px;
      cursor: pointer;
      transition: all 0.3s ease;
      font-weight: 500;
    }

    .tab-btn.active {
      background: #3498db;
      color: white;
    }

    .tab-content {
      display: none;
    }

    .tab-content.active {
      display: block;
    }

    .paper-list {
      list-style: none;
    }

    .paper-item {
      padding: 1rem;
      border-bottom: 1px solid #eee;
      transition: background-color 0.3s ease;
    }

    .paper-item:hover {
      background-color: #f8f9fa;
    }

    .paper-title {
      color: #2c3e50;
      font-weight: 600;
      margin-bottom: 0.5rem;
      text-decoration: none;
      display: block;
    }

    .paper-title:hover {
      color: #3498db;
    }

    .paper-meta {
      color: #7f8c8d;
      font-size: 0.9rem;
    }

    /* 新闻列表样式 */
    .news-list {
      list-style: none;
    }

    .news-item {
      padding: 1rem 0;
      border-bottom: 1px solid #eee;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .news-date {
      color: #3498db;
      font-weight: 600;
      min-width: 100px;
    }

    .news-title {
      flex: 1;
      margin-left: 1rem;
    }

    /* 征稿通知样式 */
    .call-for-papers {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 1.5rem;
    }

    .cfp-card {
      background: linear-gradient(135deg, #f8f9fa, #ecf0f1);
      padding: 1.5rem;
      border-radius: 8px;
      border-left: 4px solid #e74c3c;
    }

    .cfp-title {
      color: #2c3e50;
      margin-bottom: 1rem;
    }

    .cfp-deadline {
      color: #e74c3c;
      font-weight: 600;
      margin-bottom: 0.5rem;
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

      .editorial-board {
        grid-template-columns: 1fr;
      }

      .editor-header {
        flex-direction: column;
        text-align: center;
      }

      .editor-photo {
        margin-right: 0;
        margin-bottom: 1rem;
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
        <li><a href="index" class="active">首页</a></li>
        <li><a href="about">关于期刊</a></li>
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
    <h1 class="page-title">欢迎来到国际人工智能研究</h1>
    <p class="page-subtitle">International Artificial Intelligence Research - 推动人工智能前沿研究</p>
  </div>
</header>

<div class="container">
  <!-- 期刊介绍 -->
  <section class="section">
    <h2 class="section-title">期刊介绍</h2>
    <p>《国际人工智能研究》(International Artificial Intelligence Research, IAIR)是一本国际性的同行评审学术期刊，致力于发表人工智能领域的高质量原创研究成果。期刊创刊于2010年，现为月刊出版。</p>

    <h3 style="color: #2c3e50; margin: 1.5rem 0 1rem 0;">期刊指标</h3>
    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem; margin-top: 1rem;">
      <div style="text-align: center; padding: 1rem; background: #ecf0f1; border-radius: 8px;">
        <div style="font-size: 2rem; color: #3498db; font-weight: bold;">8.5</div>
        <div>影响因子 (2024)</div>
      </div>
      <div style="text-align: center; padding: 1rem; background: #ecf0f1; border-radius: 8px;">
        <div style="font-size: 2rem; color: #3498db; font-weight: bold;">15</div>
        <div>天平均初审时间</div>
      </div>
      <div style="text-align: center; padding: 1rem; background: #ecf0f1; border-radius: 8px;">
        <div style="font-size: 2rem; color: #3498db; font-weight: bold;">89%</div>
        <div>作者满意度</div>
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

  <!-- 论文列表 -->
  <section class="section">
    <h2 class="section-title">热门论文</h2>

    <div class="paper-tabs">
      <button class="tab-btn active" onclick="showTab('latest')">最新发表</button>
      <button class="tab-btn" onclick="showTab('cited')">高被引论文</button>
      <button class="tab-btn" onclick="showTab('downloaded')">最多下载</button>
      <button class="tab-btn" onclick="showTab('popular')">最受欢迎</button>
    </div>

    <!-- 最新发表 -->
    <div id="latest" class="tab-content active">
      <ul class="paper-list">
        <li class="paper-item">
          <a href="#" class="paper-title">基于Transformer的多模态学习框架研究</a>
          <div class="paper-meta">作者: 张伟明, 李静 | 发表日期: 2024-12-01 | 下载次数: 234</div>
        </li>
        <li class="paper-item">
          <a href="#" class="paper-title">联邦学习中的隐私保护机制分析</a>
          <div class="paper-meta">作者: 王建国, 陈晓华 | 发表日期: 2024-11-28 | 下载次数: 189</div>
        </li>
      </ul>
    </div>

    <!-- 高被引论文 -->
    <div id="cited" class="tab-content">
      <ul class="paper-list">
        <li class="paper-item">
          <a href="#" class="paper-title">深度强化学习在游戏AI中的应用综述</a>
          <div class="paper-meta">引用次数: 156 | 发表时间: 2023-06-15</div>
        </li>
        <li class="paper-item">
          <a href="#" class="paper-title">生成对抗网络的数学理论基础</a>
          <div class="paper-meta">引用次数: 132 | 发表时间: 2023-03-20</div>
        </li>
      </ul>
    </div>

    <!-- 最多下载 -->
    <div id="downloaded" class="tab-content">
      <ul class="paper-list">
        <li class="paper-item">
          <a href="#" class="paper-title">大语言模型的训练优化技术</a>
          <div class="paper-meta">下载次数: 3456 | 发表时间: 2024-08-10</div>
        </li>
        <li class="paper-item">
          <a href="#" class="paper-title">自动驾驶中的视觉感知算法</a>
          <div class="paper-meta">下载次数: 2876 | 发表时间: 2024-07-15</div>
        </li>
      </ul>
    </div>

    <!-- 最受欢迎 -->
    <div id="popular" class="tab-content">
      <ul class="paper-list">
        <li class="paper-item">
          <a href="#" class="paper-title">人工智能伦理与治理框架研究</a>
          <div class="paper-meta">阅读次数: 5678 | 发表时间: 2024-10-05</div>
        </li>
        <li class="paper-item">
          <a href="#" class="paper-title">量子机器学习的前沿进展</a>
          <div class="paper-meta">阅读次数: 4321 | 发表时间: 2024-09-12</div>
        </li>
      </ul>
    </div>
  </section>

  <!-- 新闻列表 -->
  <section class="section">
    <h2 class="section-title">最新新闻</h2>
    <ul class="news-list">
      <li class="news-item">
        <span class="news-date">2024-12-01</span>
        <span class="news-title">期刊影响因子提升至8.5，创历史新高</span>
        <a href="#" style="color: #3498db;">详情</a>
      </li>
      <li class="news-item">
        <span class="news-date">2024-11-25</span>
        <span class="news-title">2025年度特刊征稿通知发布</span>
        <a href="#" style="color: #3498db;">详情</a>
      </li>
      <li class="news-item">
        <span class="news-date">2024-11-15</span>
        <span class="news-title">期刊入选SCI核心数据库</span>
        <a href="#" style="color: #3498db;">详情</a>
      </li>
    </ul>
  </section>

  <!-- 征稿通知 -->
  <section class="section">
    <h2 class="section-title">征稿通知 (Call for Papers)</h2>
    <div class="call-for-papers">
      <!-- 动态生成征稿通知 -->
      <c:forEach var="cfp" items="${callForPapers}">
        <div class="cfp-card">
          <h3 class="cfp-title">${cfp.title}</h3>
          <div class="cfp-deadline">${cfp.deadline}</div>
          <p>${cfp.description}</p>
        </div>
      </c:forEach>
    </div>
  </section>
</div>

<!-- 页脚 -->
<footer style="background: #2c3e50; color: white; text-align: center; padding: 2rem; margin-top: 3rem;">
  <p>&copy; 2024 国际人工智能研究期刊. 保留所有权利.</p>
  <p>联系方式: contact@iair-journal.org | 电话: +86-10-62345678</p>
</footer>

<script>
  // 选项卡切换功能
  function showTab(tabName) {
    // 隐藏所有选项卡内容
    document.querySelectorAll('.tab-content').forEach(tab => {
      tab.classList.remove('active');
    });

    // 移除所有选项卡按钮的active类
    document.querySelectorAll('.tab-btn').forEach(btn => {
      btn.classList.remove('active');
    });

    // 显示选中的选项卡内容
    document.getElementById(tabName).classList.add('active');

    // 激活选中的选项卡按钮
    event.target.classList.add('active');
  }

  // 页面加载时初始化
  document.addEventListener('DOMContentLoaded', function() {
    // 可以在这里添加更多的交互功能
  });
</script>
</body>
</html>