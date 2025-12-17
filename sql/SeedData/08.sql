INSERT INTO Files (FileName, FilePath, UploadTime, ManuscriptID, UploaderID)
VALUES
-- 投稿相关的文件
('投稿模板.docx', '/uploads/templates/submission_template.docx', '2024-01-10 09:00:00', NULL, 2),
('版权转让协议.pdf', '/uploads/templates/copyright_form.pdf', '2024-01-10 09:05:00', NULL, 2),
('审稿意见模板.docx', '/uploads/templates/review_template.docx', '2024-01-10 09:10:00', NULL, 2),

-- 稿件1的相关文件
('深度学习图像识别论文.pdf', '/uploads/manuscripts/1/original_v1.pdf', '2024-03-01 10:00:00', 1, 6),
('稿件1匿名版.pdf', '/uploads/manuscripts/1/anonymous_v1.pdf', '2024-03-01 10:01:00', 1, 6),
('cover_letter_1.pdf', '/uploads/manuscripts/1/cover_letter_v1.pdf', '2024-03-01 10:02:00', 1, 6),
('修改说明_稿件1.pdf', '/uploads/manuscripts/1/revision_notes_v2.pdf', '2024-03-20 14:30:00', 1, 6),

-- 稿件3的相关文件
('图神经网络推荐系统.pdf', '/uploads/manuscripts/3/original_v1.pdf', '2024-02-15 09:30:00', 3, 6),
('审稿意见回复_稿件3.pdf', '/uploads/manuscripts/3/review_response.pdf', '2024-03-10 11:20:00', 3, 6),

-- 稿件8的相关文件
('纳米材料制备.pdf', '/uploads/manuscripts/8/original_v1.pdf', '2024-01-20 09:00:00', 8, 6),
('补充实验数据_稿件8.zip', '/uploads/manuscripts/8/supplementary_data.zip', '2024-02-20 10:15:00', 8, 6),

-- 稿件9的相关文件
('Transformer文本生成.pdf', '/uploads/manuscripts/9/original_v1.pdf', '2024-01-10 14:30:00', 9, 6),
('修改稿_稿件9.pdf', '/uploads/manuscripts/9/revised_v2.pdf', '2024-02-05 16:20:00', 9, 6),

-- 稿件13的相关文件
('钙钛矿太阳能电池.pdf', '/uploads/manuscripts/13/original_v1.pdf', '2024-02-25 09:40:00', 13, 10),
('补充实验_稿件13.xlsx', '/uploads/manuscripts/13/supplementary_experiments.xlsx', '2024-03-15 14:10:00', 13, 10),
('第二轮修改稿_13.pdf', '/uploads/manuscripts/13/revised_v3.pdf', '2024-03-25 10:30:00', 13, 10),

-- 稿件14的相关文件
('知识图谱问答系统.pdf', '/uploads/manuscripts/14/original_v1.pdf', '2024-01-30 10:10:00', 14, 6),
('数据集_稿件14.zip', '/uploads/manuscripts/14/dataset.zip', '2024-01-30 10:12:00', 14, 6),

-- 稿件16的相关文件
('柔性电子材料研究.pdf', '/uploads/manuscripts/16/original_v1.pdf', '2024-02-10 16:00:00', 16, 6),
('材料性能测试报告.pdf', '/uploads/manuscripts/16/test_report.pdf', '2024-02-10 16:01:00', 16, 6),

-- 稿件17的相关文件
('医学图像分割算法.pdf', '/uploads/manuscripts/17/original_v1.pdf', '2024-01-25 14:15:00', 17, 7),
('医学图像数据集_17.zip', '/uploads/manuscripts/17/medical_images.zip', '2024-01-25 14:16:00', 17, 7),

-- 新闻相关的附件
('2024投稿指南.pdf', '/uploads/news/2024_guide.pdf', '2024-01-15 14:35:00', NULL, 2),
('特刊征稿通知.pdf', '/uploads/news/special_issue.pdf', '2024-01-20 10:05:00', NULL, 2),
('编委会会议纪要.pdf', '/uploads/news/editorial_meeting.pdf', '2024-02-21 10:25:00', NULL, 2),

-- 用户个人文件
('个人简历_张三.pdf', '/uploads/users/6/cv.pdf', '2024-02-12 10:00:00', NULL, 6),
('研究成果列表_李四.pdf', '/uploads/users/7/publications.pdf', '2024-02-18 11:00:00', NULL, 7),

-- 编辑上传的文件
('稿件分配记录.xlsx', '/uploads/editors/assignment_records.xlsx', '2024-03-01 09:00:00', NULL, 4),
('审稿人名单_AI领域.pdf', '/uploads/editors/reviewer_list_ai.pdf', '2024-03-05 10:00:00', NULL, 4);