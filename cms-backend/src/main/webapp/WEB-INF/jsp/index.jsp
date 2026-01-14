<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>首页 - 国际人工智能研究</title>
  <style>
    /* 原有的CSS样式保持不变 */
    * { margin: 0; padding: 0; box-sizing: border-box; }
    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; background-color: #f8f9fa; }
    .container { max-width: 1200px; margin: 0 auto; padding: 0 20px; }

    /* 导航栏样式 */
    .navbar { background: linear-gradient(135deg, #2c3e50, #34495e); color: white; padding: 1rem 0; box-shadow: 0 2px 10px rgba(0,0,0,0.1); position: sticky; top: 0; z-index: 1000; }
    .nav-container { display: flex; justify-content: space-between; align-items: center; }
    .logo { font-size: 1.8rem; font-weight: bold; color: white; text-decoration: none; }
    .logo span { color: #3498db; }
    .nav-links { display: flex; list-style: none; gap: 2rem; }
    .nav-links a { color: white; text-decoration: none; font-weight: 500; transition: color 0.3s ease; padding: 0.5rem 1rem; border-radius: 4px; }
    .nav-links a:hover, .nav-links a.active { background-color: rgba(255,255,255,0.1); color: #3498db; }

    /* 页面标题 */
    .page-header { background: linear-gradient(135deg, #3498db, #2980b9); color: white; padding: 4rem 0; text-align: center; margin-bottom: 3rem; }
    .page-title { font-size: 2.5rem; margin-bottom: 1rem; font-weight: 300; }
    .page-subtitle { font-size: 1.2rem; opacity: 0.9; font-weight: 300; }

    /* 内容区块样式 */
    .section { background: white; margin-bottom: 2rem; padding: 2.5rem; border-radius: 10px; box-shadow: 0 2px 20px rgba(0,0,0,0.1); }
    .section-title { color: #2c3e50; font-size: 1.8rem; margin-bottom: 1.5rem; padding-bottom: 0.5rem; border-bottom: 3px solid #3498db; display: inline-block; }

    /* 编委介绍样式 */
    .editorial-board { display: grid; grid-template-columns: repeat(auto-fit, minmax(350px, 1fr)); gap: 2rem; margin-top: 2rem; }
    .editor-card { background: #f8f9fa; border-radius: 10px; padding: 1.5rem; transition: transform 0.3s ease, box-shadow 0.3s ease; border-left: 4px solid #3498db; }
    .editor-card:hover { transform: translateY(-5px); box-shadow: 0 5px 25px rgba(0,0,0,0.15); }
    .editor-header { display: flex; align-items: center; margin-bottom: 1rem; }
    .editor-photo { width: 100px; height: 100px; border-radius: 50%; object-fit: cover; background: white; display: flex; align-items: center; justify-content: center; font-size: 2.5rem; color: #7f8c8d; }
    .editor-info h3 { color: #2c3e50; margin-bottom: 0.5rem; }
    .editor-role { color: #3498db; font-weight: 600; font-size: 0.9rem; }
    .editor-bio { color: #666; line-height: 1.6; }

    /* 论文列表样式 */
    .paper-tabs { display: flex; gap: 1rem; margin-bottom: 2rem; flex-wrap: wrap; }
    .tab-btn { padding: 0.8rem 1.5rem; background: #ecf0f1; border: none; border-radius: 25px; cursor: pointer; transition: all 0.3s ease; font-weight: 500; }
    .tab-btn.active { background: #3498db; color: white; }
    .tab-content { display: none; }
    .tab-content.active { display: block; }
    .paper-list { list-style: none; }
    .paper-item { padding: 1rem; border-bottom: 1px solid #eee; transition: background-color 0.3s ease; cursor: pointer; position: relative; }
    .paper-item:hover { background-color: #f8f9fa; }
    .paper-title { color: #2c3e50; font-weight: 600; margin-bottom: 0.5rem; text-decoration: none; display: block; }
    .paper-title:hover { color: #3498db; }
    .paper-meta { color: #7f8c8d; font-size: 0.85rem; line-height: 1.4; display: flex; flex-wrap: wrap; gap: 0.8rem; }

    /* 新闻列表样式 */
    .news-list { list-style: none; }
    .news-item { padding: 1.2rem 0; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: flex-start; transition: background-color 0.3s ease; cursor: pointer; }
    .news-item:hover { background-color: #f8f9fa; padding-left: 10px; padding-right: 10px; margin: 0 -10px; border-radius: 5px; }
    .news-date { color: #3498db; font-weight: 600; min-width: 120px; font-size: 0.9rem; }
    .news-content { flex: 1; margin-left: 1.5rem; }
    .news-title { font-weight: 600; color: #2c3e50; margin-bottom: 0.5rem; display: block; text-decoration: none; }
    .news-title:hover { color: #3498db; }
    .news-preview { color: #666; font-size: 0.9rem; line-height: 1.4; }
    .news-more { color: #3498db; text-decoration: none; font-size: 0.9rem; white-space: nowrap; margin-left: 1rem; }
    .news-more:hover { text-decoration: underline; }

    /* 征稿通知样式 */
    .call-for-papers { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 1.5rem; }
    .cfp-card { background: linear-gradient(135deg, #f8f9fa, #ecf0f1); padding: 1.5rem; border-radius: 8px; border-left: 4px solid #e74c3c; transition: transform 0.3s ease, box-shadow 0.3s ease; }
    .cfp-card:hover { transform: translateY(-3px); box-shadow: 0 5px 15px rgba(0,0,0,0.1); }
    .cfp-title { color: #2c3e50; margin-bottom: 1rem; font-size: 1.2rem; }
    .cfp-deadline { color: #e74c3c; font-weight: 600; margin-bottom: 0.8rem; font-size: 0.9rem; }
    .editor-photo img { width: 100px; height: 100px; border-radius: 50%; object-fit: cover; border: 3px solid #3498db; margin: 0 auto 1rem; background: #e0e0e0; display: flex; align-items: center; justify-content: center; font-size: 2.5rem; color: white; }
    .cfp-content { color: #555; line-height: 1.5; margin-bottom: 1rem; }

    /* 文件下载样式 */
    .file-list { margin-top: 1rem; border-top: 1px solid #ddd; padding-top: 1rem; }
    .file-item { display: flex; align-items: center; justify-content: space-between; padding: 0.5rem 0; border-bottom: 1px solid #f0f0f0; }
    .file-item:last-child { border-bottom: none; }
    .file-info { display: flex; align-items: center; gap: 0.5rem; }
    .file-icon { color: #3498db; font-size: 1.2rem; }
    .file-name { color: #555; font-size: 0.9rem; }
    .download-btn { background: #27ae60; color: white; border: none; padding: 0.3rem 0.8rem; border-radius: 4px; cursor: pointer; font-size: 0.8rem; text-decoration: none; transition: background-color 0.3s ease; }
    .download-btn:hover { background: #219653; text-decoration: none; color: white; }
    .no-files { color: #7f8c8d; font-style: italic; font-size: 0.9rem; text-align: center; padding: 1rem; }

    /* 响应式设计 */
    @media (max-width: 768px) {
      .nav-container { flex-direction: column; gap: 1rem; }
      .nav-links { flex-wrap: wrap; justify-content: center; gap: 1rem; }
      .editorial-board { grid-template-columns: 1fr; }
      .editor-header { flex-direction: column; text-align: center; }
      .editor-photo { margin-right: 0; margin-bottom: 1rem; }
      .news-item { flex-direction: column; align-items: flex-start; }
      .news-content { margin-left: 0; margin-top: 0.5rem; }
      .news-more { margin-left: 0; margin-top: 0.5rem; align-self: flex-end; }
      .file-item { flex-direction: column; align-items: flex-start; gap: 0.5rem; }
      .download-btn { align-self: flex-end; }
    }

    /* 新闻详情默认隐藏，点击时展开 */
    .news-details {
      color: #666;
      font-size: 0.9rem;
      line-height: 1.4;
      margin-top: 0.5rem;
      padding: 0;
      background-color: transparent;
      max-height: 0;
      overflow: hidden;
      transition: all 0.3s ease;
      opacity: 0;
    }

    .news-details.expanded {
      padding: 0.5rem;
      background-color: #f8f9fa;
      border-radius: 4px;
      border-left: 3px solid #3498db;
      margin-top: 1rem;
      max-height: 500px; /* 足够大的值来容纳内容 */
      opacity: 1;
    }

    /* 新闻切换图标 */
    .news-toggle {
      color: #3498db;
      font-size: 0.9rem;
      cursor: pointer;
      margin-left: 1rem;
      user-select: none;
      transition: transform 0.3s ease;
      min-width: 20px;
      text-align: center;
    }

    /* 响应式调整 */
    @media (max-width: 768px) {
      .news-item {
        flex-direction: column;
        align-items: flex-start;
      }

      .news-content {
        margin-left: 0;
        margin-top: 0.5rem;
      }

      .news-toggle {
        margin-left: 0;
        margin-top: 0.5rem;
        align-self: flex-end;
      }
    }

    /* 无数据提示样式 */
    .no-data { text-align: center; padding: 40px; color: #666; font-style: italic; }

    /* 论文详情样式 */
    .paper-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 0.5rem;
    }

    .paper-toggle {
      color: #3498db;
      font-size: 0.8rem;
      margin-left: 1rem;
      transition: transform 0.3s ease;
      user-select: none;
      min-width: 20px;
      text-align: center;
    }

    .paper-abstract {
      color: #666;
      font-size: 0.9rem;
      line-height: 1.5;
      margin-top: 0.8rem;
      padding: 1rem;
      background: linear-gradient(135deg, #f8f9fa, #ecf0f1);
      border-radius: 8px;
      border-left: 4px solid #3498db;
      max-height: 0;
      overflow: hidden;
      opacity: 0;
      transition: all 0.3s ease;
      transform: translateY(-10px);
    }

    .paper-item.expanded {
      background-color: #f8f9fa;
      padding-bottom: 1.5rem;
    }

    .paper-item.expanded .paper-abstract {
      max-height: 500px;
      opacity: 1;
      transform: translateY(0);
      margin-top: 1rem;
    }

    .paper-item.expanded .paper-toggle {
      transform: rotate(180deg);
    }

    .paper-meta span {
      display: inline-flex;
      align-items: center;
      gap: 0.3rem;
    }

    /* 响应式调整 */
    @media (max-width: 768px) {
      .paper-header {
        flex-direction: column;
      }

      .paper-toggle {
        margin-left: 0;
        margin-top: 0.5rem;
        align-self: flex-end;
      }

      .paper-meta {
        flex-direction: column;
        gap: 0.3rem;
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
        <li><a href="http://localhost:5173/login">登录/注册</a></li>
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
                <div class="editor-photo" style="background: #3498db; color: white; display: flex; align-items: center; justify-content: center; font-size: 1.5rem; font-weight: bold;">
                  <c:choose>
                    <c:when test="${not empty editor.PhotoUrl}">
                      <!-- 如果有照片URL，显示照片 -->
                      <img src="${editor.PhotoUrl}" alt="${editor.FullName}" onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">
                      <!-- 备用显示：首字母 -->
                      <span style="display: none;">${editor.FullName.charAt(0)}</span>
                    </c:when>
                    <c:otherwise>
                      <!-- 如果没有照片URL，显示首字母 -->
                      <span>${editor.FullName.charAt(0)}</span>
                    </c:otherwise>
                  </c:choose>
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
        <li class="paper-item" onclick="togglePaperDetails(this)">
          <div class="paper-header">
            <a href="javascript:void(0)" class="paper-title" onclick="event.stopPropagation()">基于Transformer的多模态学习框架研究</a>
            <span class="paper-toggle">▼</span>
          </div>
          <div class="paper-meta">作者: 张伟明, 李静 | 发表日期: 2024-12-01 | 下载次数: 234 | 引用次数: 89</div>
          <div class="paper-abstract">
            <strong>摘要：</strong>本文提出了一种基于Transformer架构的多模态学习方法，通过跨模态注意力机制实现了图像、文本和语音数据的有效融合。实验结果表明，该方法在多个多模态任务中达到了最先进的性能，相比传统方法在准确率上提升了15%以上。本文还探讨了模型的可解释性问题，为多模态学习提供了新的理论支持。
          </div>
        </li>
        <li class="paper-item" onclick="togglePaperDetails(this)">
          <div class="paper-header">
            <a href="javascript:void(0)" class="paper-title" onclick="event.stopPropagation()">联邦学习中的隐私保护机制分析</a>
            <span class="paper-toggle">▼</span>
          </div>
          <div class="paper-meta">作者: 王建国, 陈晓华 | 发表日期: 2024-11-28 | 下载次数: 189 | 引用次数: 67</div>
          <div class="paper-abstract">
            <strong>摘要：</strong>随着数据隐私保护需求的日益增长，联邦学习作为一种分布式机器学习框架受到广泛关注。本文系统分析了现有联邦学习中的隐私保护机制，包括差分隐私、同态加密和安全多方计算等。提出了一种基于自适应噪声注入的差分隐私保护方法，在保证模型性能的同时显著提升了隐私保护强度。在多个真实数据集上的实验验证了方法的有效性。
          </div>
        </li>
      </ul>
    </div>
    <!-- 高被引论文 -->
    <div id="cited" class="tab-content">
      <ul class="paper-list">
        <li class="paper-item" onclick="togglePaperDetails(this)">
          <div class="paper-header">
            <a href="javascript:void(0)" class="paper-title" onclick="event.stopPropagation()">深度强化学习在游戏AI中的应用综述</a>
            <span class="paper-toggle">▼</span>
          </div>
          <div class="paper-meta">作者: 李明, 张华, 王强 | 发表日期: 2023-06-15 | 引用次数: 156 | 下载次数: 345</div>
          <div class="paper-abstract">
            <strong>摘要：</strong>本文全面综述了深度强化学习在游戏人工智能领域的最新进展和应用。涵盖了从Atari游戏到复杂策略游戏的多种应用场景，详细分析了各类深度强化学习算法的优缺点。特别讨论了模型泛化、样本效率和多智能体协同等关键挑战，并指出了未来可能的研究方向。该综述为研究者提供了全面的技术路线图。
          </div>
        </li>
        <li class="paper-item" onclick="togglePaperDetails(this)">
          <div class="paper-header">
            <a href="javascript:void(0)" class="paper-title" onclick="event.stopPropagation()">生成对抗网络的数学理论基础</a>
            <span class="paper-toggle">▼</span>
          </div>
          <div class="paper-meta">作者: 陈晓, 刘伟 | 发表日期: 2023-03-20 | 引用次数: 132 | 下载次数: 289</div>
          <div class="paper-abstract">
            <strong>摘要：</strong>本文从数学理论的角度深入探讨了生成对抗网络的收敛性、稳定性和泛化能力。提出了基于最优传输理论的GAN理论框架，从理论上解释了模式崩溃问题的根源。建立了GAN训练过程中的纳什均衡存在性定理，为改进GAN训练算法提供了理论基础。该研究在多个标准数据集上验证了理论分析的正确性。
          </div>
        </li>
      </ul>
    </div>
    <!-- 最多下载 -->
    <div id="downloaded" class="tab-content">
      <ul class="paper-list">
        <li class="paper-item" onclick="togglePaperDetails(this)">
          <div class="paper-header">
            <a href="javascript:void(0)" class="paper-title" onclick="event.stopPropagation()">大语言模型的训练优化技术</a>
            <span class="paper-toggle">▼</span>
          </div>
          <div class="paper-meta">作者: 赵明, 孙丽 | 发表日期: 2024-08-10 | 下载次数: 3456 | 引用次数: 213</div>
          <div class="paper-abstract">
            <strong>摘要：</strong>随着大语言模型规模的不断增大，其训练面临着巨大的计算资源和时间成本挑战。本文提出了几种针对大语言模型的高效训练优化技术，包括梯度检查点优化、混合精度训练优化和分布式训练策略优化。实验表明，所提方法可以将千亿参数模型的训练时间缩短40%，同时保持模型的性能。本文还探讨了不同优化技术在能耗和模型质量之间的权衡。
          </div>
        </li>
        <li class="paper-item" onclick="togglePaperDetails(this)">
          <div class="paper-header">
            <a href="javascript:void(0)" class="paper-title" onclick="event.stopPropagation()">自动驾驶中的视觉感知算法</a>
            <span class="paper-toggle">▼</span>
          </div>
          <div class="paper-meta">作者: 周涛, 吴斌 | 发表日期: 2024-07-15 | 下载次数: 2876 | 引用次数: 178</div>
          <div class="paper-abstract">
            <strong>摘要：</strong>本文针对自动驾驶中的视觉感知问题，提出了一种基于多尺度注意力机制的实时目标检测与分割算法。该算法在保持高精度的同时，能够在嵌入式设备上实现实时处理。特别设计了针对小目标检测的增强模块，在复杂道路场景下表现优异。在标准数据集上的测试结果显示，该算法在检测精度和实时性方面都优于现有方法。
          </div>
        </li>
      </ul>
    </div>
    <!-- 最受欢迎 -->
    <div id="popular" class="tab-content">
      <ul class="paper-list">
        <li class="paper-item" onclick="togglePaperDetails(this)">
          <div class="paper-header">
            <a href="javascript:void(0)" class="paper-title" onclick="event.stopPropagation()">人工智能伦理与治理框架研究</a>
            <span class="paper-toggle">▼</span>
          </div>
          <div class="paper-meta">作者: 伦理学AI研究组 | 发表日期: 2024-10-05 | 阅读次数: 5678 | 引用次数: 98</div>
          <div class="paper-abstract">
            <strong>摘要：</strong>随着人工智能技术的快速发展，其伦理问题和治理机制日益受到关注。本文系统研究了人工智能伦理的关键问题，包括算法公平性、透明性、可解释性和责任归属等。提出了一个多层次的人工智能治理框架，涵盖了技术标准、法律法规、行业自律和社会监督等多个维度。该框架已在多个实际应用场景中得到验证，为负责任的AI发展提供了参考。
          </div>
        </li>
        <li class="paper-item" onclick="togglePaperDetails(this)">
          <div class="paper-header">
            <a href="javascript:void(0)" class="paper-title" onclick="event.stopPropagation()">量子机器学习的前沿进展</a>
            <span class="paper-toggle">▼</span>
          </div>
          <div class="paper-meta">作者: 量子计算实验室 | 发表日期: 2024-09-12 | 阅读次数: 4321 | 引用次数: 67</div>
          <div class="paper-abstract">
            <strong>摘要：</strong>量子机器学习是量子计算和机器学习交叉领域的前沿研究方向。本文综述了量子机器学习的最新进展，包括量子神经网络、量子支持向量机、量子主成分分析等核心算法。深入分析了量子机器学习在计算复杂度、泛化能力和实际应用方面的优势和挑战。特别探讨了在近期量子设备上的实现方案，为量子机器学习的发展指明了方向。
          </div>
        </li>
      </ul>
    </div>
  </section>

  <!-- 最新新闻（动态展示，无附件的新闻） -->
  <section class="section">
    <h2 class="section-title">最新新闻</h2>
    <div class="news-list">
      <c:choose>
        <c:when test="${not empty newsWithoutFiles}">
          <c:forEach var="news" items="${newsWithoutFiles}">
            <div class="news-item" onclick="toggleNewsItem(${news.newsId})">
                        <span class="news-date">
                            <c:choose>
                              <c:when test="${not empty news.publishDate}">
                                ${news.publishDate}
                              </c:when>
                              <c:otherwise>
                                日期待定
                              </c:otherwise>
                            </c:choose>
                        </span>
              <div class="news-content">
                <a href="javascript:void(0)" class="news-title">${news.title}</a>
                <!-- 默认隐藏新闻详情 -->
                <div class="news-details" id="news-details-${news.newsId}">
                  <div class="news-preview">
                    <c:choose>
                      <c:when test="${not empty news.content}">
                        ${news.content}
                      </c:when>
                      <c:otherwise>
                        暂无内容描述
                      </c:otherwise>
                    </c:choose>
                  </div>
                </div>
              </div>
              <span class="news-toggle" id="news-toggle-${news.newsId}">▼</span>
            </div>
          </c:forEach>
        </c:when>
        <c:otherwise>
          <!-- 如果没有新闻数据，显示提示信息 -->
          <div class="no-data">
            <p>暂无最新新闻</p>
          </div>
        </c:otherwise>
      </c:choose>
    </div>
  </section>

  <!-- 征稿通知（动态展示，有附件的新闻） -->
  <section class="section">
    <h2 class="section-title">征稿通知 (Call for Papers)</h2>
    <div class="call-for-papers">
      <c:choose>
        <c:when test="${not empty newsWithFiles}">
          <c:forEach var="news" items="${newsWithFiles}">
            <div class="cfp-card">
              <div class="cfp-header" style="display: flex; justify-content: space-between; align-items: center; cursor: pointer;" onclick="toggleCfpContent(this)">
                <div>
                  <h3 class="cfp-title">${news.title}</h3>
                  <div class="cfp-deadline">
                    <c:choose>
                      <c:when test="${not empty news.publishDate}">
                        发布时间: ${news.publishDate}
                      </c:when>
                      <c:otherwise>
                        发布时间: 待定
                      </c:otherwise>
                    </c:choose>
                  </div>
                </div>
                <span class="cfp-toggle-icon" style="font-size: 1.2rem;">▼</span>
              </div>
              <div class="cfp-content-wrapper" style="display: none;">
                <div class="cfp-content">
                  <p>
                    <c:choose>
                      <c:when test="${not empty news.content}">
                        ${news.content}
                      </c:when>
                      <c:otherwise>
                        暂无详细内容描述
                      </c:otherwise>
                    </c:choose>
                  </p>
                </div>
                <!-- 文件下载部分 -->
                <div class="file-list">
                  <c:choose>
                    <c:when test="${not empty news.files}">
                      <c:forEach var="file" items="${news.files}">
                        <div class="file-item">
                          <div class="file-info">
                            <span class="file-icon">📎</span>
                            <span class="file-name">${file.fileName}</span>
                          </div>
                          <a href="/files/download/${file.fileId}" class="download-btn" download="${file.fileName}">
                            下载
                          </a>
                        </div>
                      </c:forEach>
                    </c:when>
                    <c:otherwise>
                      <div class="no-files">暂无附件</div>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>
            </div>
          </c:forEach>
        </c:when>
        <c:otherwise>
          <!-- 如果没有征稿数据，显示提示信息 -->
          <div class="no-data">
            <p>暂无征稿通知</p>
          </div>
        </c:otherwise>
      </c:choose>
    </div>
  </section>
</div>

<!-- 页脚 -->
<footer style="background: #2c3e50; color: white; text-align: center; padding: 2rem; margin-top: 3rem;">
  <p>&copy; 2024 国际人工智能研究期刊. 保留所有权利.</p>
  <p>联系方式: contact@iair-journal.org | 电话: +86-10-62345678</p>
</footer>

<script>
  // 切换新闻项目的展开/收缩
  function toggleNewsItem(newsId) {
    const detailsDiv = document.getElementById('news-details-' + newsId);
    const toggleIcon = document.getElementById('news-toggle-' + newsId);

    if (detailsDiv.classList.contains('expanded')) {
      // 收缩
      detailsDiv.classList.remove('expanded');
      toggleIcon.textContent = '▼';
    } else {
      // 展开
      detailsDiv.classList.add('expanded');
      toggleIcon.textContent = '▲';
    }
  }

  // 切换论文详情显示/隐藏
  function togglePaperDetails(element) {
    // 切换当前项目的展开状态
    element.classList.toggle('expanded');

    // 更新切换按钮
    const toggleIcon = element.querySelector('.paper-toggle');
    if (element.classList.contains('expanded')) {
      toggleIcon.textContent = '▲';
    } else {
      toggleIcon.textContent = '▼';
    }
  }

  // 初始化时隐藏所有新闻详情
  document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.news-details').forEach(detail => {
      detail.classList.remove('expanded');
    });
  });

  function toggleCfpContent(element) {
    const wrapper = element.parentElement.querySelector('.cfp-content-wrapper');
    const icon = element.querySelector('.cfp-toggle-icon');
    if (wrapper.style.display === 'none') {
      wrapper.style.display = 'block';
      icon.textContent = '▲';
    } else {
      wrapper.style.display = 'none';
      icon.textContent = '▼';
    }
  }

  // 更新原来的选项卡切换函数，添加收缩所有展开的内容
  function showTab(tabName) {
    // 收缩所有已展开的论文详情
    document.querySelectorAll('.paper-item.expanded').forEach(item => {
      item.classList.remove('expanded');
      const toggleIcon = item.querySelector('.paper-toggle');
      if (toggleIcon) {
        toggleIcon.textContent = '▼';
      }
    });

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