-- 插入真实期刊数据（覆盖AI、计算机、材料、医学等主流领域）
INSERT INTO Journal (
    Name,
    Introduction,
    ImpactFactor,
    Timeline,
    JournalLink,
    JournalImageLink
)
VALUES
-- 1. 人工智能领域顶刊
(
    'Artificial Intelligence',
    '创刊于1970年，由Elsevier出版，是人工智能领域的旗舰期刊，涵盖AI基础理论、算法、系统及应用等全领域研究，是该领域最具权威性的期刊之一。',
    14.707,
    '一审周期约3-4个月，录用后在线发表约1个月，印刷版发表约2-3个月；全年出版18期，投稿至最终发表平均周期约6-8个月。',
    'https://www.journals.elsevier.com/artificial-intelligence',
    'https://sciencedirect.elseviercdn.cn/shared-assets/24/images/elsevier-non-solus-new-grey.svg'
),
-- 2. 计算机视觉顶刊
(
    'IEEE Transactions on Pattern Analysis and Machine Intelligence',
    'IEEE计算机学会旗下顶刊，专注于模式识别、计算机视觉、机器学习等领域，是计算机视觉领域的顶级期刊，JCR Q1区。',
    24.314,
    '初审意见约4-6周，同行评审约2-3个月，修改后再审约1-2个月，录用后在线发表约1个月；全年12期，整体发表周期约8-12个月。',
    'https://ieeexplore.ieee.org/journal/34',
    'https://www.ieee.org/themes/custom/ieee/pattern_lab/source/images/logos/logo-ieee-blue.svg'
),
-- 3. 材料科学顶刊
(
    'Advanced Materials',
    'Wiley出版的材料科学旗舰期刊，涵盖纳米材料、功能材料、先进复合材料等前沿研究，2024年影响因子位列材料科学综合领域TOP 1%。',
    32.086,
    '投稿后1周内完成编辑初审，同行评审约3-4周，录用后2周内在线首发（Early View），正式刊期发表约1-2个月；全年52期（周刊）。',
    'https://onlinelibrary.wiley.com/journal/15214095',
    'https://advanced.onlinelibrary.wiley.com/pb-assets/hub-assets/advanced/logo-header-1764088009430.png'
),
-- 4. 医学顶刊（综合）
(
    'New England Journal of Medicine',
    '创刊于1812年，全球最具影响力的综合医学期刊，发表原创性临床研究、综述、社论等，覆盖所有医学领域，影响因子长期位居医学综合领域榜首。',
    119.308,
    '初审约1-2周，同行评审约3-4周，重大研究可加急处理（1-2周），录用后在线发表约1周，印刷版发表约1个月；每周出版1期。',
    'https://www.nejm.org/',
    'https://www.nejm.org/specs/products/mms-nextgen/mms/releasedAssets/images/other-images/nejm-logo-166667e91992a5212bc723b03e45d39f.svg'
),
-- 5. 环境科学顶刊
(
    'Environmental Science & Technology',
    'ACS出版的环境科学顶级期刊，聚焦环境污染物的检测、迁移转化、治理技术及环境政策研究，JCR环境科学领域Q1区。',
    11.357,
    '编辑初审约2周，同行评审约4-6周，修改后再审约2-3周，录用后在线发表约1个月；每月出版1期，全年12期。',
    'https://pubs.acs.org/journal/esthag',
    'https://pubs.acs.org/action/showCoverImage?journalCode=esthag&showLarge=true'
),
-- 6. 经济学顶刊
(
    'The Quarterly Journal of Economics',
    '牛津大学出版社出版，哈佛大学经济系主办，是经济学领域最具权威性的期刊之一，发表宏观经济学、微观经济学、计量经济学等原创研究。',
    20.808,
    '初审约1-2个月，同行评审约3-4个月，录用率约5%，录用后在线发表约2个月，正式刊期发表约3-4个月；全年4期（季刊）。',
    'https://academic.oup.com/qje',
    'https://oup.silverchair-cdn.com/oup/backfile/Content_public/Journal/qje/Issue/140/4/1/m_cover.jpeg?Expires=1768996432&Signature=QAh9ZXxaIBnbhJbRXunFdnXSif~YDqmFuW3cpyk33~XGM1GV5twWJWAImK5jY-PRPo0A6BcJy2wC2KSM2Znevqhtom0nVTyJ0sE2SDHa2ujpEiRiVlyGim-6wT0o3GlziRMrqEPGLZxpzEyTsmMI2zGU-pgT7SPsO9o79qd-kBg6YdjeO0Ucecl3glwhQHlZgMQtxH46wHd4Qr7vnQ-E9rbaBaf-Kj6bFdbo0s5mgMTnSwCaANDmJ19GVWGwCWs3-x-qpsgvmahuFJ5gLfFQX7z4GHdSI-xCDLSm4HMOMb4S-jlgIJkd1sjtI7F2Up-9BjmS-OGLnBh255jPcryCog__&Key-Pair-Id=APKAIE5G5CRDK6RD3PGA'
),
-- 7. 化学顶刊
(
    'Angewandte Chemie International Edition',
    'Wiley出版的化学领域顶级期刊，涵盖有机化学、无机化学、物理化学、分析化学等全领域，以快速发表创新性研究著称。',
    16.823,
    '编辑初审约1周，同行评审约2-3周，加急审稿可在1周内完成，录用后在线发表约3-5天；每周出版1期，全年52期。',
    'https://onlinelibrary.wiley.com/journal/15213773',
    'https://onlinelibrary.wiley.com/cms/asset/2f143561-273e-4575-a9f9-31e9e9667c37/anie.v64.51.cover.gif'
),
-- 8. 物理学顶刊
(
    'Physical Review Letters',
    '美国物理学会（APS）出版，是物理学领域最具影响力的快报类期刊，发表物理学各分支的重要原创性研究成果，短篇快报形式为主。',
    9.161,
    '初审约1周，同行评审约2-3周，录用后在线发表约1周，正式刊期发表约2周；每周出版1期，全年52期。',
    'https://journals.aps.org/prl',
    'https://journals.aps.org/images/aps-logo.svg?1765837821'
);
GO