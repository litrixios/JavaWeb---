INSERT INTO Users (Username, Password, Role, Email, FullName, Affiliation, ResearchDirection, RegisterTime, Status,AvatarUrl)
VALUES
    ('admin', '123', 'SuperAdmin', 'superadmin@journal.edu', '张明', '期刊管理系统总部', '系统管理、期刊出版管理', '2024-01-15 09:00:00', 1,NULL),
    ('editorinchief', '123456', 'EditorInChief', '2662775049@qq.com', '李华', '清华大学计算机科学与技术学院', '人工智能、计算机视觉、期刊编辑管理', '2024-01-20 10:30:00', 1,NULL),
    ('editorialadmin1', '123456', 'EditorialAdmin', 'admin.li@journal.edu', '王芳', '期刊管理办公室', '稿件流程管理、作者服务', '2024-01-25 14:20:00', 1,NULL),
    ('editor_ai', '123456', 'Editor', '2662775049@qq.com', '陈强', '北京大学人工智能研究院', '机器学习、深度学习、自然语言处理', '2024-02-01 11:00:00', 1,NULL),
    ('editor_material', '123456', 'Editor', '2662775049@qq.com', '刘伟', '中国科学院材料研究所', '纳米材料、功能材料、复合材料', '2024-02-05 15:30:00', 1,NULL),
    ('prof_zhang', '123456', 'Author', '2662775049@qq.com', '张建国', '复旦大学计算机科学学院', '数据挖掘、推荐系统、人工智能', '2024-02-10 09:45:00', 1,NULL),
    ('phd_student', '123456', 'Author', 'wang.phd@nankai.edu.cn', '王晓明', '南开大学数学科学学院', '计算数学、优化算法、数值分析', '2024-02-15 13:20:00', 1,NULL),
    ('reviewer_expert', '123456', 'Reviewer', 'review.expert@zju.edu.cn', '周涛', '浙江大学计算机学院', '计算机视觉、图像处理、模式识别', '2024-02-20 16:10:00', 1,NULL),
    ('young_reviewer', '123456', 'Reviewer', 'li.young@whu.edu.cn', '李娜', '武汉大学信息管理学院', '信息检索、文本挖掘、数字图书馆', '2024-02-25 10:00:00', 1,NULL),
    ('systemadmin', '123456', 'SystemAdmin', 'new.author@nju.edu.cn', '赵新宇', '南京大学物理学院', '凝聚态物理、量子计算、材料物理', '2024-03-01 14:00:00', 1,NULL);
GO