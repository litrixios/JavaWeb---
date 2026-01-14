<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>子刊浏览 - 国际人工智能研究</title>
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
      padding: 3rem 0;
      text-align: center;
      margin-bottom: 2rem;
    }

    .page-title {
      font-size: 2.2rem;
      margin-bottom: 0.5rem;
      font-weight: 300;
    }

    .page-subtitle {
      font-size: 1.1rem;
      opacity: 0.9;
      font-weight: 300;
    }

    /* 内容区域样式 */
    .section {
      background: white;
      margin-bottom: 2rem;
      padding: 2rem;
      border-radius: 10px;
      box-shadow: 0 2px 20px rgba(0,0,0,0.1);
    }

    .section-title {
      color: #2c3e50;
      font-size: 1.6rem;
      margin-bottom: 1.5rem;
      padding-bottom: 0.5rem;
      border-bottom: 3px solid #3498db;
      display: inline-block;
    }

    /* 子刊列表样式 */
    .journals-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 1.5rem;
      margin-top: 1.5rem;
    }

    .journal-card {
      background: #f8f9fa;
      border-radius: 10px;
      padding: 1.5rem;
      transition: transform 0.3s ease, box-shadow 0.3s ease;
      border-left: 4px solid #3498db;
      cursor: pointer;
      height: 100%;
    }

    .journal-card:hover {
      transform: translateY(-5px);
      box-shadow: 0 5px 25px rgba(0,0,0,0.15);
    }

    .journal-cover {
      width: 100%;
      height: 150px;
      background: linear-gradient(135deg, #3498db, #2980b9);
      border-radius: 8px;
      margin-bottom: 1rem;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 1.8rem;
      font-weight: bold;
    }

    .journal-title {
      color: #2c3e50;
      font-size: 1.2rem;
      margin-bottom: 0.5rem;
      min-height: 3rem;
    }

    .journal-meta {
      color: #7f8c8d;
      font-size: 0.9rem;
      margin-bottom: 0.5rem;
    }

    .journal-description {
      color: #666;
      font-size: 0.9rem;
      line-height: 1.4;
      margin-bottom: 1rem;
      min-height: 4rem;
    }

    .journal-articles {
      color: #3498db;
      font-size: 0.9rem;
      font-weight: 500;
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

      .journals-grid {
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
    <h1 class="page-title">子刊浏览</h1>
    <p class="page-subtitle">探索国际人工智能研究期刊下的各类专业子刊</p>
  </div>
</header>

<div class="container">

  <!-- 子刊列表区域 -->
  <section class="section" id="journals-section">
    <h2 class="section-title">全部子刊</h2>

    <div class="journals-grid">
      <div class="journal-card" onclick="showJournalDetails('AIT')">
        <div class="journal-cover">AIT</div>
        <h3 class="journal-title">人工智能理论与方法</h3>
        <div class="journal-meta">主编: 张伟教授 | ISSN: 2096-1234</div>
        <div class="journal-description">专注于人工智能基础理论、算法设计和数学方法的研究，推动AI领域的理论创新。</div>
      </div>

      <div class="journal-card" onclick="showJournalDetails('MLA')">
        <div class="journal-cover">MLA</div>
        <h3 class="journal-title">机器学习与应用</h3>
        <div class="journal-meta">主编: 李静教授 | ISSN: 2096-1235</div>
        <div class="journal-description">关注机器学习算法在实际问题中的应用，包括工业、商业和社会领域的创新应用。</div>
      </div>

      <div class="journal-card" onclick="showJournalDetails('CVP')">
        <div class="journal-cover">CVP</div>
        <h3 class="journal-title">计算机视觉与模式识别</h3>
        <div class="journal-meta">主编: 王建国教授 | ISSN: 2096-1236</div>
        <div class="journal-description">发表计算机视觉、图像处理、模式识别和多媒体分析领域的前沿研究成果。</div>
      </div>

      <div class="journal-card" onclick="showJournalDetails('NLP')">
        <div class="journal-cover">NLP</div>
        <h3 class="journal-title">自然语言处理与计算语言学</h3>
        <div class="journal-meta">主编: 陈晓华教授 | ISSN: 2096-1237</div>
        <div class="journal-description">专注于自然语言处理、机器翻译、情感分析和计算语言学的基础与应用研究。</div>
      </div>

      <div class="journal-card" onclick="showJournalDetails('RAI')">
        <div class="journal-cover">RAI</div>
        <h3 class="journal-title">机器人与人工智能系统</h3>
        <div class="journal-meta">主编: 赵琳教授 | ISSN: 2096-1238</div>
        <div class="journal-description">发表机器人学、智能控制系统、自主系统和智能硬件等领域的研究成果。</div>
      </div>

      <div class="journal-card" onclick="showJournalDetails('AIH')">
        <div class="journal-cover">AIH</div>
        <h3 class="journal-title">人工智能与医疗健康</h3>
        <div class="journal-meta">主编: 刘强教授 | ISSN: 2096-1239</div>
        <div class="journal-description">关注AI在医疗诊断、药物研发、健康管理和生物信息学中的应用与创新。</div>
      </div>

      <div class="journal-card" onclick="showJournalDetails('AIE')">
        <div class="journal-cover" style="background: linear-gradient(135deg, #9b59b6, #8e44ad);">AIE</div>
        <h3 class="journal-title">人工智能伦理与社会影响</h3>
        <div class="journal-meta">主编: 孙正义教授 | ISSN: 2096-1240</div>
        <div class="journal-description">探讨人工智能的伦理问题、社会责任、政策影响和可持续发展。</div>
      </div>

      <div class="journal-card" onclick="showJournalDetails('EAI')">
        <div class="journal-cover" style="background: linear-gradient(135deg, #e74c3c, #c0392b);">EAI</div>
        <h3 class="journal-title">边缘计算与人工智能</h3>
        <div class="journal-meta">主编: 周计算教授 | ISSN: 2096-1241</div>
        <div class="journal-description">研究边缘智能、物联网AI、分布式学习和低功耗AI算法。</div>
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
  // 子刊数据
  var journalsData = {
    'AIT': {
      title: '人工智能理论与方法',
      abbreviation: 'AIT',
      issn: '2096-1234',
      editor: '张伟 教授',
      description: '《人工智能理论与方法》专注于人工智能的基础理论研究、算法设计和数学方法创新。本刊旨在推动AI领域的理论发展，发表高质量的原创性理论研究论文。',
      scope: [
        '机器学习理论',
        '深度学习基础',
        '优化算法',
        '强化学习理论',
        '概率图模型',
        '计算学习理论'
      ],
      latestArticles: [
        {
          title: '深度神经网络的泛化能力分析',
          authors: '张三, 李四, 王五',
          date: '2024-12-15',
          link: '#'
        },
        {
          title: '非凸优化的新算法研究',
          authors: '赵六, 钱七',
          date: '2024-12-10',
          link: '#'
        }
      ],
      statistics: {
        totalArticles: 245,
        articlesThisYear: 45,
        impactFactor: 6.8
      }
    },
    'MLA': {
      title: '机器学习与应用',
      abbreviation: 'MLA',
      issn: '2096-1235',
      editor: '李静 教授',
      description: '《机器学习与应用》关注机器学习算法在工业、商业和社会领域的实际应用。本刊发表具有实际应用价值的机器学习研究成果。',
      scope: [
        '工业机器学习',
        '商业智能',
        '推荐系统',
        '预测分析',
        '异常检测',
        '时序分析'
      ],
      latestArticles: [
        {
          title: '基于深度学习的金融风控系统',
          authors: '王明, 陈红',
          date: '2024-12-20',
          link: '#'
        },
        {
          title: '智能制造中的质量预测模型',
          authors: '刘洋, 张华',
          date: '2024-12-18',
          link: '#'
        }
      ],
      statistics: {
        totalArticles: 312,
        articlesThisYear: 68,
        impactFactor: 7.2
      }
    },
    'CVP': {
      title: '计算机视觉与模式识别',
      abbreviation: 'CVP',
      issn: '2096-1236',
      editor: '王建国 教授',
      description: '《计算机视觉与模式识别》发表计算机视觉、图像处理、模式识别和多媒体分析领域的前沿研究成果。',
      scope: [
        '目标检测与识别',
        '图像分割',
        '三维重建',
        '人脸识别',
        '视频分析',
        '医学影像分析'
      ],
      latestArticles: [
        {
          title: '多视角三维物体重建新方法',
          authors: '陈建国, 李伟',
          date: '2024-12-22',
          link: '#'
        },
        {
          title: '实时视频目标跟踪算法',
          authors: '张勇, 王芳',
          date: '2024-12-19',
          link: '#'
        }
      ],
      statistics: {
        totalArticles: 198,
        articlesThisYear: 42,
        impactFactor: 8.1
      }
    },
    'NLP': {
      title: '自然语言处理与计算语言学',
      abbreviation: 'NLP',
      issn: '2096-1237',
      editor: '陈晓华 教授',
      description: '《自然语言处理与计算语言学》专注于自然语言处理、机器翻译、情感分析和计算语言学的基础与应用研究。',
      scope: [
        '机器翻译',
        '情感分析',
        '信息抽取',
        '问答系统',
        '文本生成',
        '语义理解'
      ],
      latestArticles: [
        {
          title: '大语言模型在机器翻译中的应用',
          authors: '刘涛, 周明',
          date: '2024-12-25',
          link: '#'
        },
        {
          title: '跨语言情感分析研究',
          authors: '赵芳, 钱伟',
          date: '2024-12-21',
          link: '#'
        }
      ],
      statistics: {
        totalArticles: 267,
        articlesThisYear: 56,
        impactFactor: 7.5
      }
    },
    'RAI': {
      title: '机器人与人工智能系统',
      abbreviation: 'RAI',
      issn: '2096-1238',
      editor: '赵琳 教授',
      description: '《机器人与人工智能系统》发表机器人学、智能控制系统、自主系统和智能硬件等领域的研究成果。',
      scope: [
        '机器人控制',
        '自主导航',
        '人机交互',
        '智能制造',
        '无人机系统',
        '智能硬件'
      ],
      latestArticles: [
        {
          title: '仿生机器人的运动控制研究',
          authors: '孙强, 李娜',
          date: '2024-12-16',
          link: '#'
        },
        {
          title: '工业机器人智能编程系统',
          authors: '王刚, 陈静',
          date: '2024-12-14',
          link: '#'
        }
      ],
      statistics: {
        totalArticles: 176,
        articlesThisYear: 34,
        impactFactor: 6.3
      }
    },
    'AIH': {
      title: '人工智能与医疗健康',
      abbreviation: 'AIH',
      issn: '2096-1239',
      editor: '刘强 教授',
      description: '《人工智能与医疗健康》关注AI在医疗诊断、药物研发、健康管理和生物信息学中的应用与创新。',
      scope: [
        '医学影像分析',
        '疾病预测',
        '药物发现',
        '健康监测',
        '基因组学',
        '精准医疗'
      ],
      latestArticles: [
        {
          title: '深度学习在癌症早期诊断中的应用',
          authors: '周华, 吴明',
          date: '2024-12-28',
          link: '#'
        },
        {
          title: 'AI辅助药物分子设计',
          authors: '郑伟, 王芳',
          date: '2024-12-24',
          link: '#'
        }
      ],
      statistics: {
        totalArticles: 223,
        articlesThisYear: 52,
        impactFactor: 8.9
      }
    },
    'AIE': {
      title: '人工智能伦理与社会影响',
      abbreviation: 'AIE',
      issn: '2096-1240',
      editor: '孙正义 教授',
      description: '《人工智能伦理与社会影响》探讨人工智能的伦理问题、社会责任、政策影响和可持续发展。',
      scope: [
        'AI伦理框架',
        '算法公平性',
        '隐私保护',
        'AI政策研究',
        '社会影响评估',
        '可持续发展'
      ],
      latestArticles: [
        {
          title: 'AI决策的伦理审查机制',
          authors: '李道德, 王正义',
          date: '2024-12-17',
          link: '#'
        },
        {
          title: '算法偏见的社会影响研究',
          authors: '张公平, 陈平',
          date: '2024-12-13',
          link: '#'
        }
      ],
      statistics: {
        totalArticles: 45,
        articlesThisYear: 23,
        impactFactor: 5.2
      }
    },
    'EAI': {
      title: '边缘计算与人工智能',
      abbreviation: 'EAI',
      issn: '2096-1241',
      editor: '周计算 教授',
      description: '《边缘计算与人工智能》研究边缘智能、物联网AI、分布式学习和低功耗AI算法。',
      scope: [
        '边缘智能',
        '物联网AI',
        '分布式学习',
        '模型压缩',
        '低功耗算法',
        '实时推理'
      ],
      latestArticles: [
        {
          title: '边缘设备上的轻量级模型部署',
          authors: '钱边缘, 孙计算',
          date: '2024-12-23',
          link: '#'
        },
        {
          title: '物联网中的联邦学习优化',
          authors: '赵联网, 李物',
          date: '2024-12-20',
          link: '#'
        }
      ],
      statistics: {
        totalArticles: 32,
        articlesThisYear: 18,
        impactFactor: 4.8
      }
    }
  };

  // 显示子刊详情
  function showJournalDetails(journalId) {
    var journal = journalsData[journalId];
    if (journal) {
      // 创建详情模态框
      var modal = document.createElement('div');
      modal.style.cssText = 'position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.8); display: flex; justify-content: center; align-items: center; z-index: 2000; padding: 20px;';

      var scopeList = '';
      for (var i = 0; i < journal.scope.length; i++) {
        scopeList += '<li style="background: #f8f9fa; padding: 0.5rem 1rem; border-radius: 4px; color: #666;">• ' + journal.scope[i] + '</li>';
      }

      var articlesList = '';
      for (var i = 0; i < journal.latestArticles.length; i++) {
        var article = journal.latestArticles[i];
        articlesList += '<div style="padding: 0.5rem 0; border-bottom: 1px solid #eee;">' +
                '<div style="font-weight: 500; color: #2c3e50;">' + article.title + '</div>' +
                '<div style="color: #7f8c8d; font-size: 0.9rem;">作者: ' + article.authors + ' | 发表日期: ' + article.date + '</div>' +
                '</div>';
      }

      modal.innerHTML = '<div style="background: white; border-radius: 10px; padding: 2rem; max-width: 800px; width: 100%; max-height: 90vh; overflow-y: auto;">' +
              '<div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem;">' +
              '<h2 style="color: #2c3e50; font-size: 1.8rem;">' + journal.title + '</h2>' +
              '<button onclick="this.parentElement.parentElement.parentElement.remove()" style="background: none; border: none; font-size: 1.5rem; cursor: pointer; color: #7f8c8d;">&times;</button>' +
              '</div>' +

              '<div style="display: flex; gap: 2rem; margin-bottom: 2rem; flex-wrap: wrap;">' +
              '<div style="flex: 1; min-width: 200px;">' +
              '<div style="background: linear-gradient(135deg, #3498db, #2980b9); height: 150px; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: white; font-size: 2rem; font-weight: bold; margin-bottom: 1rem;">' +
              journal.abbreviation +
              '</div>' +
              '</div>' +
              '<div style="flex: 2; min-width: 300px;">' +
              '<div style="margin-bottom: 1rem;"><strong>ISSN:</strong> ' + journal.issn + '</div>' +
              '<div style="margin-bottom: 1rem;"><strong>主编:</strong> ' + journal.editor + '</div>' +
              '<div style="margin-bottom: 1rem;"><strong>影响因子:</strong> ' + journal.statistics.impactFactor + '</div>' +
              '<div style="margin-bottom: 1rem;"><strong>总文章数:</strong> ' + journal.statistics.totalArticles + '篇</div>' +
              '<div><strong>今年文章:</strong> ' + journal.statistics.articlesThisYear + '篇</div>' +
              '</div>' +
              '</div>' +

              '<div style="margin-bottom: 2rem;">' +
              '<h3 style="color: #2c3e50; margin-bottom: 1rem; border-bottom: 2px solid #3498db; padding-bottom: 0.5rem;">期刊简介</h3>' +
              '<p style="line-height: 1.6; color: #666;">' + journal.description + '</p>' +
              '</div>' +

              '<div style="margin-bottom: 2rem;">' +
              '<h3 style="color: #2c3e50; margin-bottom: 1rem; border-bottom: 2px solid #3498db; padding-bottom: 0.5rem;">研究范围</h3>' +
              '<ul style="display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 0.5rem; list-style: none; padding: 0;">' +
              scopeList +
              '</ul>' +
              '</div>' +

              '<div>' +
              '<h3 style="color: #2c3e50; margin-bottom: 1rem; border-bottom: 2px solid #3498db; padding-bottom: 0.5rem;">最新文章</h3>' +
              '<div style="background: #f8f9fa; padding: 1rem; border-radius: 8px;">' +
              articlesList +
              '</div>' +
              '</div>' +

              '<div style="margin-top: 2rem; display: flex; justify-content: flex-end; gap: 1rem;">' +
              '<button onclick="this.parentElement.parentElement.parentElement.remove()" style="padding: 0.5rem 1.5rem; background: #e74c3c; color: white; border: none; border-radius: 4px; cursor: pointer;">关闭</button>' +
              '<button onclick="window.open(\'#\', \'_blank\')" style="padding: 0.5rem 1.5rem; background: #3498db; color: white; border: none; border-radius: 4px; cursor: pointer;">查看详情</button>' +
              '</div>' +
              '</div>';

      document.body.appendChild(modal);

      // 点击模态框外部关闭
      modal.addEventListener('click', function(e) {
        if (e.target === modal) {
          modal.remove();
        }
      });
    }
  }

  // 搜索功能
  document.getElementById('searchInput').addEventListener('input', function() {
    var searchTerm = this.value.toLowerCase();
    var journalCards = document.querySelectorAll('.journal-card');

    for (var i = 0; i < journalCards.length; i++) {
      var card = journalCards[i];
      var title = card.querySelector('.journal-title').textContent.toLowerCase();
      var description = card.querySelector('.journal-description').textContent.toLowerCase();

      if (title.includes(searchTerm) || description.includes(searchTerm)) {
        card.style.display = 'block';
      } else {
        card.style.display = 'none';
      }
    }
  });

  // 筛选功能
  document.getElementById('categoryFilter').addEventListener('change', function() {
    var category = this.value;
    // 这里可以根据选择的分类进行筛选
    if (category === '') {
      // 显示所有子刊
      var journalCards = document.querySelectorAll('.journal-card');
      for (var i = 0; i < journalCards.length; i++) {
        journalCards[i].style.display = 'block';
      }
    }
  });
</script>
</body>
</html>