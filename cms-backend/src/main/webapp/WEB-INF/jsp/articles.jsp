<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>文章与专刊 - 国际人工智能研究</title>
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

    /* 内容区域样式 */
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

    /* 期刊分类选项卡 */
    .issue-tabs {
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
      color: #7f8c8d;
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

    /* 期刊列表样式 */
    .issues-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 1.5rem;
      margin-top: 1.5rem;
    }

    .issue-card {
      background: #f8f9fa;
      border-radius: 10px;
      padding: 1.5rem;
      transition: transform 0.3s ease, box-shadow 0.3s ease;
      border-left: 4px solid #3498db;
      cursor: pointer;
    }

    .issue-card:hover {
      transform: translateY(-5px);
      box-shadow: 0 5px 25px rgba(0,0,0,0.15);
    }

    .issue-cover {
      width: 100%;
      height: 150px;
      background: linear-gradient(135deg, #3498db, #2980b9);
      border-radius: 8px;
      margin-bottom: 1rem;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 2rem;
      font-weight: bold;
    }

    .issue-title {
      color: #2c3e50;
      font-size: 1.2rem;
      margin-bottom: 0.5rem;
    }

    .issue-meta {
      color: #7f8c8d;
      font-size: 0.9rem;
      margin-bottom: 0.5rem;
    }

    .issue-articles {
      color: #3498db;
      font-size: 0.9rem;
      font-weight: 500;
    }

    /* 文章列表样式 */
    .articles-list {
      list-style: none;
      margin-top: 2rem;
    }

    .article-item {
      padding: 1.5rem;
      border-bottom: 1px solid #eee;
      transition: background-color 0.3s ease;
    }

    .article-item:hover {
      background-color: #f8f9fa;
    }

    .article-title {
      color: #2c3e50;
      font-size: 1.2rem;
      margin-bottom: 0.5rem;
      text-decoration: none;
      display: block;
    }

    .article-title:hover {
      color: #3498db;
    }

    .article-authors {
      color: #7f8c8d;
      margin-bottom: 0.5rem;
      font-size: 0.9rem;
    }

    .article-abstract {
      color: #666;
      margin-bottom: 1rem;
      font-size: 0.9rem;
      line-height: 1.5;
    }

    .article-meta {
      display: flex;
      justify-content: space-between;
      color: #7f8c8d;
      font-size: 0.8rem;
    }

    .article-actions {
      display: flex;
      gap: 1rem;
    }

    .article-action {
      color: #3498db;
      text-decoration: none;
      font-size: 0.8rem;
    }

    .article-action:hover {
      text-decoration: underline;
    }

    /* 搜索和筛选 */
    .search-filter {
      display: flex;
      gap: 1rem;
      margin-bottom: 2rem;
      flex-wrap: wrap;
    }

    .search-box {
      flex: 1;
      min-width: 300px;
      position: relative;
    }

    .search-input {
      width: 100%;
      padding: 0.8rem 1rem 0.8rem 2.5rem;
      border: 1px solid #ddd;
      border-radius: 5px;
      font-size: 1rem;
    }

    .search-icon {
      position: absolute;
      left: 0.8rem;
      top: 50%;
      transform: translateY(-50%);
      color: #7f8c8d;
    }

    .filter-select {
      padding: 0.8rem 1rem;
      border: 1px solid #ddd;
      border-radius: 5px;
      background: white;
      min-width: 150px;
    }

    /* 返回按钮 */
    .back-btn {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      color: #3498db;
      text-decoration: none;
      margin-bottom: 1.5rem;
      font-weight: 500;
    }

    .back-btn:hover {
      text-decoration: underline;
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

      .issues-grid {
        grid-template-columns: 1fr;
      }

      .search-filter {
        flex-direction: column;
      }

      .search-box {
        min-width: 100%;
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
        <li><a href="submit">论文发表</a></li>
        <li><a href="articles" class="active">文章与专刊</a></li>
        <li><a href="guide">用户指南</a></li>
        <li><a href="http://localhost:5173/login">登录/注册</a></li>
      </ul>
    </div>
  </div>
</nav>

<!-- 页面标题 -->
<header class="page-header">
  <div class="container">
    <h1 class="page-title">文章与专刊</h1>
    <p class="page-subtitle">探索国际人工智能研究期刊的已发表论文和特刊</p>
  </div>
</header>

<div class="container">

  <!-- 期刊列表区域 -->
  <section class="section" id="issues-section">
    <h2 class="section-title">期刊列表</h2>

    <div class="issue-tabs">
      <button class="tab-btn active" onclick="showTab('latest')">最新期刊 (Latest Issues)</button>
      <button class="tab-btn" onclick="showTab('special')">特刊 (Special Issues)</button>
      <button class="tab-btn" onclick="showTab('all')">所有期刊 (All Issues)</button>
    </div>

    <!-- 最新期刊 -->
    <div id="latest" class="tab-content active">
      <div class="issues-grid">
        <div class="issue-card" onclick="showArticles('issue-2024-12')">
          <div class="issue-cover">IAIR Vol.15 No.12</div>
          <h3 class="issue-title">2024年12月期</h3>
          <div class="issue-meta">出版日期: 2024-12-01</div>
          <div class="issue-articles">收录文章: 8篇</div>
        </div>

        <div class="issue-card" onclick="showArticles('issue-2024-11')">
          <div class="issue-cover">IAIR Vol.15 No.11</div>
          <h3 class="issue-title">2024年11月期</h3>
          <div class="issue-meta">出版日期: 2024-11-01</div>
          <div class="issue-articles">收录文章: 10篇</div>
        </div>

        <div class="issue-card" onclick="showArticles('issue-2024-10')">
          <div class="issue-cover">IAIR Vol.15 No.10</div>
          <h3 class="issue-title">2024年10月期</h3>
          <div class="issue-meta">出版日期: 2024-10-01</div>
          <div class="issue-articles">收录文章: 9篇</div>
        </div>
      </div>
    </div>

    <!-- 特刊 -->
    <div id="special" class="tab-content">
      <div class="issues-grid">
        <div class="issue-card" onclick="showArticles('special-ai-health')">
          <div class="issue-cover" style="background: linear-gradient(135deg, #e74c3c, #c0392b);">特刊</div>
          <h3 class="issue-title">人工智能在医疗健康中的应用</h3>
          <div class="issue-meta">出版日期: 2024-09-15</div>
          <div class="issue-articles">收录文章: 12篇</div>
        </div>

        <div class="issue-card" onclick="showArticles('special-sustainable-ai')">
          <div class="issue-cover" style="background: linear-gradient(135deg, #2ecc71, #27ae60);">特刊</div>
          <h3 class="issue-title">可持续人工智能</h3>
          <div class="issue-meta">出版日期: 2024-06-20</div>
          <div class="issue-articles">收录文章: 9篇</div>
        </div>

        <div class="issue-card" onclick="showArticles('special-trustworthy-ai')">
          <div class="issue-cover" style="background: linear-gradient(135deg, #f39c12, #e67e22);">特刊</div>
          <h3 class="issue-title">可信人工智能</h3>
          <div class="issue-meta">出版日期: 2024-03-10</div>
          <div class="issue-articles">收录文章: 11篇</div>
        </div>
      </div>
    </div>

    <!-- 所有期刊 -->
    <div id="all" class="tab-content">
      <div class="issues-grid">
        <div class="issue-card" onclick="showArticles('issue-2024-09')">
          <div class="issue-cover">IAIR Vol.15 No.9</div>
          <h3 class="issue-title">2024年9月期</h3>
          <div class="issue-meta">出版日期: 2024-09-01</div>
          <div class="issue-articles">收录文章: 8篇</div>
        </div>

        <div class="issue-card" onclick="showArticles('issue-2024-08')">
          <div class="issue-cover">IAIR Vol.15 No.8</div>
          <h3 class="issue-title">2024年8月期</h3>
          <div class="issue-meta">出版日期: 2024-08-01</div>
          <div class="issue-articles">收录文章: 7篇</div>
        </div>

        <div class="issue-card" onclick="showArticles('issue-2024-07')">
          <div class="issue-cover">IAIR Vol.15 No.7</div>
          <h3 class="issue-title">2024年7月期</h3>
          <div class="issue-meta">出版日期: 2024-07-01</div>
          <div class="issue-articles">收录文章: 9篇</div>
        </div>

        <div class="issue-card" onclick="showArticles('issue-2024-06')">
          <div class="issue-cover">IAIR Vol.15 No.6</div>
          <h3 class="issue-title">2024年6月期</h3>
          <div class="issue-meta">出版日期: 2024-06-01</div>
          <div class="issue-articles">收录文章: 10篇</div>
        </div>
      </div>
    </div>
  </section>

  <!-- 文章列表区域 (默认隐藏) -->
  <section class="section" id="articles-section" style="display: none;">
    <a href="#" class="back-btn" onclick="showIssues()">
      <i class="fas fa-arrow-left"></i> 返回期刊列表
    </a>

    <h2 class="section-title" id="articles-title">文章列表</h2>

    <ul class="articles-list" id="articles-list">
      <!-- 文章内容将通过JavaScript动态加载 -->
    </ul>
  </section>
</div>

<!-- 页脚 -->
<footer style="background: #2c3e50; color: white; text-align: center; padding: 2rem; margin-top: 3rem;">
  <p>&copy; 2024 国际人工智能研究期刊. 保留所有权利.</p>
  <p>联系方式: contact@iair-journal.org | 电话: +86-10-62345678</p>
</footer>

<script>
  // 文章数据
  const articlesData = {
    'issue-2024-12': {
      title: '2024年12月期 - IAIR Vol.15 No.12',
      articles: [
        {
          title: '基于Transformer的多模态学习框架研究',
          authors: '张伟明, 李静, 王建国',
          abstract: '本研究提出了一种新型的多模态学习框架，通过Transformer架构有效整合视觉和文本信息，在多个基准数据集上取得了state-of-the-art的性能。',
          date: '2024-12-01',
          doi: '10.1234/iair.2024.12.001',
          pdf: '#'
        },
        {
          title: '联邦学习中的隐私保护机制分析',
          authors: '陈晓华, 赵琳, 刘强',
          abstract: '本文系统分析了联邦学习中的隐私保护技术，提出了一种基于差分隐私的改进方案，在保护用户数据隐私的同时保持了模型性能。',
          date: '2024-12-01',
          doi: '10.1234/iair.2024.12.002',
          pdf: '#'
        },
        {
          title: '大语言模型在代码生成中的应用研究',
          authors: '王编程, 李代码, 张算法',
          abstract: '探讨了大语言模型在自动代码生成和程序理解方面的应用，提出了一种结合语法约束的代码生成方法，显著提高了生成代码的质量和可读性。',
          date: '2024-12-01',
          doi: '10.1234/iair.2024.12.003',
          pdf: '#'
        }
      ]
    },
    'special-ai-health': {
      title: '特刊：人工智能在医疗健康中的应用',
      articles: [
        {
          title: '深度学习在医学影像诊断中的最新进展',
          authors: '王医学, 李影像, 张诊断',
          abstract: '综述了深度学习技术在CT、MRI等医学影像分析中的应用，讨论了当前的技术挑战和未来发展方向。',
          date: '2024-09-15',
          doi: '10.1234/iair.special.2024.001',
          pdf: '#'
        },
        {
          title: '基于自然语言处理的电子病历分析系统',
          authors: '刘文本, 陈分析, 赵系统',
          abstract: '提出了一种基于BERT的电子病历分析系统，能够自动提取关键医疗信息，辅助医生进行诊断决策。',
          date: '2024-09-15',
          doi: '10.1234/iair.special.2024.002',
          pdf: '#'
        },
        {
          title: '个性化药物推荐系统的深度学习模型',
          authors: '张药物, 李推荐, 王个性化',
          abstract: '开发了一种结合患者基因组数据和临床信息的深度学习模型，为个体化药物治疗提供精准推荐。',
          date: '2024-09-15',
          doi: '10.1234/iair.special.2024.003',
          pdf: '#'
        }
      ]
    },
    'issue-2024-11': {
      title: '2024年11月期 - IAIR Vol.15 No.11',
      articles: [
        {
          title: '强化学习在机器人控制中的新方法',
          authors: '赵机器人, 钱控制, 孙学习',
          abstract: '提出了一种基于深度强化学习的机器人控制算法，在复杂环境中表现出优越的适应性和鲁棒性。',
          date: '2024-11-01',
          doi: '10.1234/iair.2024.11.001',
          pdf: '#'
        },
        {
          title: '知识图谱在智能问答系统中的应用',
          authors: '周知识, 吴图谱, 郑问答',
          abstract: '构建了一个大规模领域知识图谱，并将其应用于智能问答系统，显著提高了问答的准确性和覆盖范围。',
          date: '2024-11-01',
          doi: '10.1234/iair.2024.11.002',
          pdf: '#'
        }
      ]
    },
    'special-sustainable-ai': {
      title: '特刊：可持续人工智能',
      articles: [
        {
          title: '绿色AI：降低深度学习模型能耗的方法研究',
          authors: '钱绿色, 孙能耗, 李可持续',
          abstract: '研究了一系列降低深度学习模型训练和推理能耗的技术，包括模型压缩、知识蒸馏和动态计算等。',
          date: '2024-06-20',
          doi: '10.1234/iair.special.2024.004',
          pdf: '#'
        },
        {
          title: 'AI在气候变化预测中的应用综述',
          authors: '周气候, 吴预测, 郑AI',
          abstract: '系统回顾了人工智能技术在气候变化建模、极端天气预测和环境影响评估中的应用现状和挑战。',
          date: '2024-06-20',
          doi: '10.1234/iair.special.2024.005',
          pdf: '#'
        }
      ]
    },
    'special-trustworthy-ai': {
      title: '特刊：可信人工智能',
      articles: [
        {
          title: '深度神经网络的可解释性方法研究',
          authors: '冯可解释, 陈可信, 褚AI',
          abstract: '提出了一种新的神经网络可视化技术，能够有效解释模型的决策过程，提高AI系统的透明度和可信度。',
          date: '2024-03-10',
          doi: '10.1234/iair.special.2024.006',
          pdf: '#'
        },
        {
          title: '对抗攻击下的深度学习模型鲁棒性分析',
          authors: '卫对抗, 沈攻击, 韩鲁棒',
          abstract: '系统分析了深度学习模型在面对各种对抗攻击时的脆弱性，并提出了一种新的防御机制。',
          date: '2024-03-10',
          doi: '10.1234/iair.special.2024.007',
          pdf: '#'
        }
      ]
    }
  };

  // 显示期刊选项卡
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

  // 显示文章列表
  function showArticles(issueId) {
    // 隐藏期刊列表，显示文章列表
    document.getElementById('issues-section').style.display = 'none';
    document.getElementById('articles-section').style.display = 'block';

    // 设置文章列表标题
    document.getElementById('articles-title').textContent = articlesData[issueId].title;

    // 清空现有文章列表
    const articlesList = document.getElementById('articles-list');
    articlesList.innerHTML = '';

    // 添加文章到列表
    articlesData[issueId].articles.forEach(article => {
      const articleItem = document.createElement('li');
      articleItem.className = 'article-item';
      articleItem.innerHTML = `
                    <a href="#" class="article-title">${article.title}</a>
                    <div class="article-authors">作者: ${article.authors}</div>
                    <div class="article-abstract">${article['abstract']}</div>
                    <div class="article-meta">
                        <span>发表日期: ${article.date} | DOI: ${article.doi}</span>
                        <div class="article-actions">
                            <a href="${article.pdf}" class="article-action">PDF全文</a>
                            <a href="#" class="article-action">引用</a>
                            <a href="#" class="article-action">分享</a>
                        </div>
                    </div>
                `;
      articlesList.appendChild(articleItem);
    });
  }

  // 返回期刊列表
  function showIssues() {
    document.getElementById('issues-section').style.display = 'block';
    document.getElementById('articles-section').style.display = 'none';
  }

  // 搜索功能
  document.getElementById('searchInput').addEventListener('input', function() {
    const searchTerm = this.value.toLowerCase();
    // 这里可以添加搜索逻辑
    console.log('搜索关键词:', searchTerm);
  });

  // 页面加载时初始化
  document.addEventListener('DOMContentLoaded', function() {
    // 可以在这里添加更多的初始化代码
  });
</script>
</body>
</html>