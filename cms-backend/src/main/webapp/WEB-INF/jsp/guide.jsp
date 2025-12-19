<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>用户指南 - 国际人工智能研究</title>
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

    .guide-menu {
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
      display: none;
    }

    .section.active {
      display: block;
    }

    .section-title {
      color: #2c3e50;
      font-size: 1.8rem;
      margin-bottom: 1.5rem;
      padding-bottom: 0.5rem;
      border-bottom: 3px solid #3498db;
      display: inline-block;
    }

    /* 指南内容样式 */
    .guide-content {
      line-height: 1.8;
    }

    .guide-content h3 {
      color: #2c3e50;
      margin: 2rem 0 1rem 0;
      font-size: 1.4rem;
    }

    .guide-content h4 {
      color: #3498db;
      margin: 1.5rem 0 0.5rem 0;
      font-size: 1.2rem;
    }

    .guide-content p {
      margin-bottom: 1rem;
      color: #555;
    }

    .guide-content ul, .guide-content ol {
      margin: 1rem 0;
      padding-left: 2rem;
    }

    .guide-content li {
      margin-bottom: 0.5rem;
    }

    .info-box {
      background: #e8f4f8;
      border-left: 4px solid #3498db;
      padding: 1.5rem;
      margin: 1.5rem 0;
      border-radius: 0 8px 8px 0;
    }

    .warning-box {
      background: #fff3cd;
      border-left: 4px solid #ffc107;
      padding: 1.5rem;
      margin: 1.5rem 0;
      border-radius: 0 8px 8px 0;
    }

    .important-box {
      background: #ffeaa7;
      border-left: 4px solid #f39c12;
      padding: 1.5rem;
      margin: 1.5rem 0;
      border-radius: 0 8px 8px 0;
    }

    .download-box {
      background: #f8f9fa;
      border: 1px solid #e9ecef;
      border-radius: 8px;
      padding: 1.5rem;
      margin: 1.5rem 0;
    }

    .download-item {
      display: flex;
      align-items: center;
      padding: 1rem;
      background: white;
      border-radius: 5px;
      margin-bottom: 1rem;
      transition: transform 0.3s ease;
    }

    .download-item:hover {
      transform: translateX(5px);
      box-shadow: 0 2px 10px rgba(0,0,0,0.1);
    }

    .download-item:last-child {
      margin-bottom: 0;
    }

    .download-icon {
      width: 50px;
      height: 50px;
      background: #3498db;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 1.5rem;
      margin-right: 1rem;
      flex-shrink: 0;
    }

    .download-content {
      flex: 1;
    }

    .download-title {
      font-weight: 600;
      color: #2c3e50;
      margin-bottom: 0.5rem;
    }

    .download-link {
      display: inline-block;
      background: #3498db;
      color: white;
      padding: 0.5rem 1rem;
      border-radius: 5px;
      text-decoration: none;
      font-size: 0.9rem;
      transition: background 0.3s ease;
    }

    .download-link:hover {
      background: #2980b9;
    }

    /* 表格样式 */
    .format-table {
      width: 100%;
      border-collapse: collapse;
      margin: 1.5rem 0;
    }

    .format-table th, .format-table td {
      border: 1px solid #ddd;
      padding: 0.8rem;
      text-align: left;
    }

    .format-table th {
      background: #f8f9fa;
      font-weight: 600;
      color: #2c3e50;
    }

    .format-table tr:nth-child(even) {
      background: #f8f9fa;
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
        <li><a href="articles">文章与专刊</a></li>
        <li><a href="guide" class="active">用户指南</a></li>
        <li><a href="login">登录/注册</a></li>
      </ul>
    </div>
  </div>
</nav>

<!-- 页面标题 -->
<header class="page-header">
  <div class="container">
    <h1 class="page-title">用户指南</h1>
    <p class="page-subtitle">为作者、审稿人和读者提供全面的期刊使用指南</p>
  </div>
</header>

<div class="container">
  <div class="main-content">
    <!-- 侧边栏菜单 -->
    <aside class="sidebar">
      <h2 class="sidebar-title">用户指南</h2>
      <ul class="guide-menu">
        <li class="menu-item">
          <a href="#about-journal" class="active" onclick="showSection('about-journal')">
            <i class="fas fa-info-circle"></i>
            <span>关于期刊</span>
          </a>
        </li>
        <li class="menu-item">
          <a href="#ethics-policies" onclick="showSection('ethics-policies')">
            <i class="fas fa-balance-scale"></i>
            <span>伦理与政策</span>
          </a>
        </li>
        <li class="menu-item">
          <a href="#writing-formatting" onclick="showSection('writing-formatting')">
            <i class="fas fa-edit"></i>
            <span>写作与格式</span>
          </a>
        </li>
        <li class="menu-item">
          <a href="#submission-process" onclick="showSection('submission-process')">
            <i class="fas fa-paper-plane"></i>
            <span>投稿流程</span>
          </a>
        </li>
        <li class="menu-item">
          <a href="#review-process" onclick="showSection('review-process')">
            <i class="fas fa-search"></i>
            <span>审稿流程</span>
          </a>
        </li>
        <li class="menu-item">
          <a href="#publication-process" onclick="showSection('publication-process')">
            <i class="fas fa-print"></i>
            <span>出版流程</span>
          </a>
        </li>
      </ul>
    </aside>

    <!-- 内容区域 -->
    <div class="content">
      <!-- 关于期刊部分 -->
      <section id="about-journal" class="section active">
        <h2 class="section-title">关于期刊</h2>
        <div class="guide-content">
          <h3>期刊简介</h3>
          <p>《国际人工智能研究》(International Artificial Intelligence Research, IAIR)是一本国际性的同行评审学术期刊，致力于发表人工智能领域的高质量原创研究成果。期刊创刊于2010年，现为月刊出版，已被SCI、EI、Scopus等知名数据库收录。</p>

          <h3>期刊宗旨</h3>
          <p>本期刊旨在推动人工智能理论、方法和技术的前沿发展，为全球研究人员提供高质量的学术交流平台。我们鼓励跨学科研究，促进人工智能在各领域的创新应用。</p>

          <h3>收录范围</h3>
          <p>期刊涵盖人工智能领域的广泛研究方向，包括但不限于：</p>
          <ul>
            <li><strong>机器学习：</strong>深度学习、强化学习、迁移学习等</li>
            <li><strong>自然语言处理：</strong>文本分析、机器翻译、情感分析等</li>
            <li><strong>计算机视觉：</strong>图像识别、目标检测、三维重建等</li>
            <li><strong>机器人学：</strong>自主导航、人机交互、智能控制等</li>
            <li><strong>知识工程：</strong>知识表示、推理、知识图谱等</li>
            <li><strong>人工智能应用：</strong>医疗健康、智慧城市、金融科技等</li>
          </ul>

          <h3>期刊指标</h3>
          <div class="info-box">
            <ul>
              <li><strong>影响因子：</strong>8.5 (2024)</li>
              <li><strong>出版频率：</strong>月刊</li>
              <li><strong>审稿周期：</strong>4-6周</li>
              <li><strong>接受率：</strong>约25%</li>
              <li><strong>在线发表：</strong>接受后立即发布</li>
            </ul>
          </div>
        </div>
      </section>

      <!-- 伦理与政策部分 -->
      <section id="ethics-policies" class="section">
        <h2 class="section-title">伦理与政策</h2>
        <div class="guide-content">
          <h3>作者伦理准则</h3>
          <p>所有投稿作者必须遵守以下伦理准则：</p>

          <h4>原创性与抄袭</h4>
          <ul>
            <li>稿件必须是原创作品，未在其他地方发表过</li>
            <li>禁止任何形式的抄袭、自我抄袭或数据造假</li>
            <li>引用他人工作必须适当标注出处</li>
          </ul>

          <h4>作者贡献</h4>
          <ul>
            <li>所有列出的作者必须对研究工作有实质性贡献</li>
            <li>通讯作者负责确保所有合著者同意稿件提交</li>
            <li>禁止荣誉作者（未对研究做出贡献的作者）</li>
          </ul>

          <h4>利益冲突声明</h4>
          <p>作者必须披露任何可能影响研究客观性的财务或非财务利益冲突。</p>

          <div class="warning-box">
            <h4>重要提醒</h4>
            <p>违反伦理准则的稿件将被立即拒稿，情节严重者可能被列入黑名单，禁止在未来一段时间内向本刊投稿。</p>
          </div>

          <h3>审稿人伦理准则</h3>
          <p>审稿人应遵守以下原则：</p>
          <ul>
            <li>客观公正地评估稿件的学术价值</li>
            <li>及时完成审稿任务</li>
            <li>对稿件内容严格保密</li>
            <li>披露任何潜在的利益冲突</li>
          </ul>

          <h3>数据共享政策</h3>
          <p>本刊鼓励作者共享研究数据以促进科学研究的可重复性：</p>
          <ul>
            <li>鼓励将数据存入公开存储库</li>
            <li>支持FAIR数据原则（可查找、可访问、可互操作、可重用）</li>
            <li>提供数据可用性声明模板</li>
          </ul>
        </div>
      </section>

      <!-- 写作与格式部分 -->
      <section id="writing-formatting" class="section">
        <h2 class="section-title">写作与格式</h2>
        <div class="guide-content">
          <h3>稿件结构要求</h3>
          <p>所有投稿稿件应包含以下部分：</p>
          <ol>
            <li><strong>标题：</strong>简明扼要，反映研究内容</li>
            <li><strong>作者信息：</strong>姓名、单位、邮箱地址</li>
            <li><strong>摘要：</strong>250-300字，概述研究目的、方法、结果和结论</li>
            <li><strong>关键词：</strong>4-6个，便于检索</li>
            <li><strong>引言：</strong>研究背景、问题和创新点</li>
            <li><strong>方法：</strong>详细描述研究方法和实验设计</li>
            <li><strong>结果：</strong>客观呈现研究结果</li>
            <li><strong>讨论：</strong>结果分析、与现有研究比较、研究局限性</li>
            <li><strong>结论：</strong>主要发现和研究意义</li>
            <li><strong>参考文献：</strong>按引用顺序编号</li>
          </ol>

          <h3>格式规范</h3>
          <table class="format-table">
            <thead>
            <tr>
              <th>项目</th>
              <th>要求</th>
              <th>说明</th>
            </tr>
            </thead>
            <tbody>
            <tr>
              <td>语言</td>
              <td>英语</td>
              <td>使用标准学术英语</td>
            </tr>
            <tr>
              <td>长度</td>
              <td>6,000-8,000词</td>
              <td>包括参考文献和图表</td>
            </tr>
            <tr>
              <td>字体</td>
              <td>Times New Roman</td>
              <td>12号字，双倍行距</td>
            </tr>
            <tr>
              <td>页边距</td>
              <td>2.5厘米</td>
              <td>上下左右一致</td>
            </tr>
            <tr>
              <td>图表</td>
              <td>高分辨率</td>
              <td>300 dpi以上，单独提交</td>
            </tr>
            </tbody>
          </table>

          <h3>参考文献格式</h3>
          <p>本刊采用IEEE引用格式：</p>
          <div class="info-box">
            <p><strong>期刊文章：</strong> [1] A. Author, "文章标题," <em>期刊名称</em>, vol. 1, no. 1, pp. 1-10, 2024.</p>
            <p><strong>会议论文：</strong> [2] B. Author, "论文标题," in <em>会议名称</em>, 地点, 2024, pp. 100-105.</p>
            <p><strong>书籍：</strong> [3] C. Author, <em>书籍标题</em>, 出版地: 出版社, 2024.</p>
          </div>

          <div class="download-box">
            <h3>模板下载</h3>
            <div class="download-item">
              <div class="download-icon">
                <i class="fas fa-file-word"></i>
              </div>
              <div class="download-content">
                <div class="download-title">Microsoft Word 模板</div>
                <p>包含完整的格式设置和样式</p>
                <a href="#" class="download-link">下载模板</a>
              </div>
            </div>
            <div class="download-item">
              <div class="download-icon">
                <i class="fas fa-code"></i>
              </div>
              <div class="download-content">
                <div class="download-title">LaTeX 模板</div>
                <p>适用于使用LaTeX的作者</p>
                <a href="#" class="download-link">下载模板</a>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 投稿流程部分 -->
      <section id="submission-process" class="section">
        <h2 class="section-title">投稿流程</h2>
        <div class="guide-content">
          <h3>投稿前准备</h3>
          <p>在提交稿件前，请确保已完成以下准备工作：</p>
          <ol>
            <li>稿件符合期刊的范围和格式要求</li>
            <li>所有作者已同意投稿并确认作者顺序</li>
            <li>已准备好Cover Letter（投稿信）</li>
            <li>已确认所有必要的声明和表格</li>
          </ol>

          <h3>在线投稿步骤</h3>
          <div class="info-box">
            <h4>第一步：注册/登录账户</h4>
            <p>访问期刊网站，注册作者账户或使用现有账户登录。</p>

            <h4>第二步：开始新投稿</h4>
            <p>在作者中心点击"提交新稿件"，选择适当的文章类型。</p>

            <h4>第三步：填写稿件信息</h4>
            <p>完整填写所有必填字段，包括标题、摘要、关键词等。</p>

            <h4>第四步：上传文件</h4>
            <p>依次上传稿件正文、图表文件、Cover Letter等。</p>

            <h4>第五步：添加作者信息</h4>
            <p>输入所有作者的详细信息，包括姓名、单位和邮箱。</p>

            <h4>第六步：确认并提交</h4>
            <p>检查所有信息无误后，确认提交稿件。</p>
          </div>

          <h3>投稿后流程</h3>
          <ul>
            <li><strong>稿件编号：</strong>提交后系统会分配唯一稿件编号</li>
            <li><strong>确认邮件：</strong>通讯作者将收到投稿确认邮件</li>
            <li><strong>状态查询：</strong>可通过作者中心查询稿件状态</li>
          </ul>
        </div>
      </section>

      <!-- 审稿流程部分 -->
      <section id="review-process" class="section">
        <h2 class="section-title">审稿流程</h2>
        <div class="guide-content">
          <h3>双盲同行评审</h3>
          <p>本刊采用双盲同行评审流程，确保评审的公正性：</p>
          <ul>
            <li>审稿人不知道作者身份</li>
            <li>作者不知道审稿人身份</li>
            <li>每篇稿件由2-3位领域专家评审</li>
          </ul>

          <h3>审稿流程阶段</h3>
          <div class="important-box">
            <h4>阶段一：编辑部初审 (1-2周)</h4>
            <p>编辑部检查稿件是否符合期刊范围和要求，进行抄袭检测。</p>

            <h4>阶段二：学术编辑评估 (1周)</h4>
            <p>学术编辑评估稿件的学术质量和创新性，决定是否送审。</p>

            <h4>阶段三：同行评审 (4-6周)</h4>
            <p>邀请2-3位审稿人进行详细评审，提供建设性意见。</p>

            <h4>阶段四：编辑决定 (1-2周)</h4>
            <p>学术编辑综合审稿意见，做出最终决定。</p>
          </div>

          <h3>可能的审稿结果</h3>
          <ul>
            <li><strong>接受：</strong>稿件被接受，将进入出版流程</li>
            <li><strong>小修后接受：</strong>需进行少量修改</li>
            <li><strong>大修后重投：</strong>需进行重大修改后重新投稿</li>
            <li><strong>拒稿：</strong>稿件未被接受</li>
          </ul>
        </div>
      </section>

      <!-- 出版流程部分 -->
      <section id="publication-process" class="section">
        <h2 class="section-title">出版流程</h2>
        <div class="guide-content">
          <h3>稿件接受后流程</h3>
          <p>稿件被接受后，将进入以下出版流程：</p>
          <ol>
            <li><strong>版权转让：</strong>作者签署版权转让协议</li>
            <li><strong>校对：</strong>作者检查排版后的稿件</li>
            <li><strong>在线发表：</strong>稿件在线优先发表</li>
            <li><strong>正式出版：</strong>纳入某一期的正式出版</li>
          </ol>

          <h3>开放获取选项</h3>
          <p>本刊提供两种出版模式：</p>
          <div class="info-box">
            <h4>传统订阅模式</h4>
            <p>无出版费，但读者需要订阅才能访问全文。</p>

            <h4>开放获取模式</h4>
            <p>支付文章处理费（APC），稿件立即免费向所有读者开放。</p>
          </div>

          <h3>文章传播</h3>
          <p>为扩大研究成果的影响力，我们鼓励作者：</p>
          <ul>
            <li>在学术社交网络分享文章链接</li>
            <li>将预印本存入相关存储库</li>
            <li>在学术会议展示研究成果</li>
          </ul>
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
  // 显示指定部分，隐藏其他部分
  function showSection(sectionId) {
    // 隐藏所有部分
    document.querySelectorAll('.section').forEach(section => {
      section.classList.remove('active');
    });

    // 显示选中的部分
    document.getElementById(sectionId).classList.add('active');

    // 更新菜单项激活状态
    document.querySelectorAll('.menu-item a').forEach(item => {
      item.classList.remove('active');
    });
    event.currentTarget.classList.add('active');

    // 平滑滚动到顶部
    window.scrollTo({
      top: 0,
      behavior: 'smooth'
    });
  }

  // 页面加载时根据URL哈希值显示对应部分
  document.addEventListener('DOMContentLoaded', function() {
    const hash = window.location.hash;
    if (hash) {
      const correspondingMenuItem = document.querySelector(`.menu-item a[href="${hash}"]`);
      if (correspondingMenuItem) {
        showSection(hash.substring(1));
      }
    }
  });
</script>
</body>
</html>