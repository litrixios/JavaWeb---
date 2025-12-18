-- 插入用户权限数据（基于任务书中各角色的权限定义）
INSERT INTO UserPermissions (UserID, CanSubmitManuscript, CanViewAllManuscripts, CanAssignReviewer, CanViewReviewerIdentity, CanWriteReview, CanMakeDecision, CanModifySystemConfig, CanTechCheck, CanPublishNews)
VALUES
-- 1. 超级管理员 (UserID=1) - 拥有所有权限
(1, 1, 1, 1, 1, 1, 1, 1, 1, 1),

-- 2. 编辑部主任/主编 (UserID=2) - 主编权限
(2, 0, 1, 1, 1, 0, 1, 0, 0, 0),

-- 3. 编辑部管理员 (UserID=3) - 编辑部管理员权限
(3, 0, 0, 0, 0, 0, 0, 0, 1, 1),

-- 4. 编辑/AI方向 (UserID=4) - 编辑权限
(4, 0, 0, 1, 1, 0, 0, 0, 0, 0),

-- 5. 编辑/材料方向 (UserID=5) - 编辑权限
(5, 0, 0, 1, 1, 0, 0, 0, 0, 0),

-- 6. 作者/教授 (UserID=6) - 作者权限
(6, 1, 0, 0, 0, 0, 0, 0, 0, 0),

-- 7. 作者/博士生 (UserID=7) - 作者权限
(7, 1, 0, 0, 0, 0, 0, 0, 0, 0),

-- 8. 审稿人/资深专家 (UserID=8) - 审稿人权限
(8, 0, 0, 0, 0, 1, 0, 0, 0, 0),

-- 9. 审稿人/青年教师 (UserID=9) - 审稿人权限
(9, 0, 0, 0, 0, 1, 0, 0, 0, 0),

-- 10. 新作者/待审核 (UserID=10) - 注册后默认无权限，审核通过后赋予作者权限
(10, 0, 0, 0, 0, 0, 0, 0, 0, 0);
GO