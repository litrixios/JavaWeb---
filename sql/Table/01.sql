-- =============================================
-- 1. 用户表 (Users)
-- 包含超级管理员、编辑、作者、审稿人等所有角色
-- =============================================
CREATE TABLE Users (
                       UserID INT PRIMARY KEY IDENTITY(1,1),      -- 用户ID，自增
                       Username NVARCHAR(50) NOT NULL UNIQUE,     -- 用户名，唯一
                       Password NVARCHAR(100) NOT NULL,           -- 密码 (建议存加密后的哈希值，课设初期可用明文)
                       Role NVARCHAR(20) NOT NULL,                -- 角色类型 (SuperAdmin, Admin, Author, Reviewer, Editor, EditorInChief, EditorialAdmin)
                       Email NVARCHAR(100) NOT NULL,              -- 邮箱
                       FullName NVARCHAR(50),                     -- 全名 (显示在页面右上角)
                       Affiliation NVARCHAR(100),                 -- 单位
                       ResearchDirection NVARCHAR(200),           -- 研究方向
                       RegisterTime DATETIME DEFAULT GETDATE(),   -- 注册时间
                       Status INT DEFAULT 0,                       -- 状态 (0: 待审核/未激活, 1: 正常, 2: 禁用)
                       AvatarUrl NVARCHAR(500) NULL                 -- 新增：存储个人头像的图片路径
);
GO

-- =============================================
-- 2. 期刊表 (Journal)
-- 存储期刊的基本信息
-- =============================================
CREATE TABLE Journal (
                         JournalID INT PRIMARY KEY IDENTITY(1,1),   -- 期刊ID
                         Name NVARCHAR(100) NOT NULL,               -- 期刊名称 (如：International Artificial Intelligence Research)
                         Introduction NVARCHAR(MAX),                -- 期刊介绍
                         ImpactFactor DECIMAL(6, 3),                -- 影响因子 (例如: 3.502)
                         Timeline NVARCHAR(MAX),                    -- 发表时间线说明
                         JournalLink NVARCHAR(255),                 -- 新增的链接属性，用于前端跳转
                         JournalImageLink NVARCHAR(666)             -- 存储期刊封面的图片 URL 链接

);
GO

-- =============================================
-- 3. 稿件表 (Manuscript)
-- 稿件的核心存储，包括元数据和当前状态
-- =============================================
CREATE TABLE Manuscript (
                            ManuscriptID INT PRIMARY KEY IDENTITY(1,1),    -- 稿件ID
                            AuthorID INT NOT NULL,                         -- 提交作者ID (外键)
                            CurrentEditorID INT NULL,                      -- 当前责任编辑ID (可能为空)
                            Title NVARCHAR(255) NOT NULL,                  -- 标题
                            Abstract NVARCHAR(MAX),                        -- 摘要
                            Keywords NVARCHAR(200),                        -- 关键词
                            AuthorList NVARCHAR(MAX),                      -- 作者列表 (文本格式存储，如 "张三, 李四")
                            FundingInfo NVARCHAR(200),                     -- 项目资助信息
                            Status NVARCHAR(50) NOT NULL DEFAULT 'Incomplete'
        CONSTRAINT CK_Manuscript_Status
        CHECK (Status IN ('Incomplete','Processing','Revision','Decided')), -- 状态大类
                            SubStatus NVARCHAR(50) NULL                     -- 子状态
        CONSTRAINT CK_Manuscript_SubStatus
        CHECK (SubStatus IN ('TechCheck','PendingDeskReview','PendingAssign',
                            'WithEditor','UnderReview','Accepted','Rejected','Retracted')),
                            SubmissionTime DATETIME DEFAULT GETDATE(),      -- 提交时间
                            Decision NVARCHAR(50),                          -- 最终决定 (Accept, Reject, Revise)
                            DecisionReason NVARCHAR(MAX) NULL,              -- 决定理由（给作者的官方通知）
                            DecisionTime DATETIME,                          -- 决定时间
                            EditorRecommendation NVARCHAR(50) NULL,         -- 编辑推荐的决策 (如: Suggest Acceptance, Suggest Rejection)
                            EditorSummaryReport NVARCHAR(MAX) NULL,         -- 编辑撰写的总结报告
                            RecommendationDate DATETIME NULL,               -- 提交推荐的日期
                            RevisionDeadline DATETIME NULL,                 -- 修改稿件截止日期

    -- 撤稿相关字段
                            RetractTime DATETIME NULL,                      -- 撤稿时间
                            RetractByUserID INT NULL,                       -- 撤稿人ID
                            RetractReason NVARCHAR(MAX) NULL,               -- 撤稿详细原因
                            RetractType NVARCHAR(50) NULL                   -- 撤稿类型（如：主动撤稿、被动撤稿、伦理问题等）
        CONSTRAINT CK_Manuscript_RetractType
        CHECK (RetractType IN ('Voluntary', 'Involuntary', 'EthicalIssue', 'DataIssue', 'Other')),

    -- 状态逻辑约束：确保主状态和子状态的组合是有效的
                            CONSTRAINT CK_Status_Logic CHECK (
                                -- 处理中状态：可以有多种子状态
                                (Status = 'Processing' AND SubStatus IN ('TechCheck', 'PendingDeskReview', 'PendingAssign', 'WithEditor', 'UnderReview')) OR
                                    -- 已决定状态：包括接受、拒绝和撤稿
                                (Status = 'Decided' AND SubStatus IN ('Accepted', 'Rejected', 'Retracted')) OR
                                    -- 修改中状态：没有子状态
                                (Status = 'Revision' AND SubStatus IS NULL) OR
                                    -- 未完成状态：没有子状态
                                (Status = 'Incomplete' AND SubStatus IS NULL)
                                ),

    -- 外键约束
                            CONSTRAINT FK_Manuscript_Author
                                FOREIGN KEY (AuthorID) REFERENCES Users(UserID),
                            CONSTRAINT FK_Manuscript_Editor
                                FOREIGN KEY (CurrentEditorID) REFERENCES Users(UserID),
                            CONSTRAINT FK_Manuscript_RetractByUser
                                FOREIGN KEY (RetractByUserID) REFERENCES Users(UserID),

    -- 业务规则约束
                            CONSTRAINT CK_Retracted_Consistency CHECK (
                                -- 如果SubStatus是Retracted，则Decision应该是Reject（通常撤稿等同于拒绝）
                                (SubStatus != 'Retracted' OR Decision = 'Reject') AND
                                    -- 如果SubStatus是Retracted，RetractTime、RetractByUserID和RetractReason应该不为空
                                (SubStatus != 'Retracted' OR
         (RetractTime IS NOT NULL AND RetractByUserID IS NOT NULL AND RetractReason IS NOT NULL))
                                ),
);
Go
-- =============================================
-- 4. 稿件版本表 (Versions)
-- 用于支持修回功能，存储稿件的不同版本
-- =============================================
CREATE TABLE Versions (
                          VersionID INT PRIMARY KEY IDENTITY(1,1),    -- 版本ID
                          ManuscriptID INT NOT NULL,                  -- 稿件ID (外键)
                          VersionNumber INT NOT NULL,                 -- 版本号 (1, 2, 3...)
                          AnonymousFilePath NVARCHAR(255),            -- 匿名版文件路径 (PDF)
                          OriginalFilePath NVARCHAR(255),             -- 原版文件路径 (PDF)
                          CoverLetterPath NVARCHAR(255),              -- Cover Letter路径
                          MarkerFilePath NVARCHAR(255),               -- 标记稿路径 （PDF）
                          ResponseLetterPath NVARCHAR(255),           -- 回复信路径 (修回时使用)
                          UploadTime DATETIME DEFAULT GETDATE(),      -- 上传时间
                          CONSTRAINT FK_Versions_Manuscript FOREIGN KEY (ManuscriptID) REFERENCES Manuscript(ManuscriptID)
);
GO

-- =============================================
-- 5. 审稿任务表 (Review)
-- 存储审稿人与稿件的关联及审稿结果
-- =============================================
CREATE TABLE Review (
                        ReviewID INT PRIMARY KEY IDENTITY(1,1),     -- 评审ID
                        ManuscriptID INT NOT NULL,                  -- 稿件ID (外键)
                        ReviewerID INT NOT NULL,                    -- 审稿人ID (外键)
                        CommentsToAuthor NVARCHAR(MAX),             -- 给作者的公开意见
                        ConfidentialComments NVARCHAR(MAX),         -- 给编辑的保密意见
                        Score INT,                                  -- 打分 (例如 1-5 分)
                        Suggestion NVARCHAR(50),                    -- 建议 (Accept, Minor Revision, Major Revision, Reject)
                        InviteDate DATETIME DEFAULT GETDATE(),      -- 邀请日期
                        Deadline DATETIME,                          -- 截止日期
                        SubmitTime DATETIME,                        -- 意见提交时间
                        Status NVARCHAR(20) DEFAULT 'Invited',      -- 状态 (Invited, Accepted, Rejected, Completed, Overdue)
                        CONSTRAINT FK_Review_Manuscript FOREIGN KEY (ManuscriptID) REFERENCES Manuscript(ManuscriptID),
                        CONSTRAINT FK_Review_Reviewer FOREIGN KEY (ReviewerID) REFERENCES Users(UserID)
);
GO

-- =============================================
-- 6. ��ί�� (Editorial_Board)
-- չʾ��ǰ̨�ı�ί��Ϣ
-- =============================================
CREATE TABLE Editorial_Board (
                                 BoardID INT PRIMARY KEY IDENTITY(1,1),      -- ��ԱID
                                 UserID INT NOT NULL,                        -- �������û�ID (���)
                                 Position NVARCHAR(50),                      -- ְλ (Editor-in-Chief, Associate Editor)
                                 Introduction NVARCHAR(MAX),                 -- ���˼��
                                 Section NVARCHAR(100),                      -- ������Ŀ/����
                                 IsVisible BIT DEFAULT 1,                    --�Ƿ���ʾ
                                 PhotoUrl NVARCHAR(256),
                                 CONSTRAINT FK_Board_User FOREIGN KEY (UserID) REFERENCES Users(UserID)
);
GO

-- =============================================
-- 7. 新闻表 (News)
-- 用于前台展示通知和公告
-- =============================================
CREATE TABLE News (
                      NewsID INT PRIMARY KEY IDENTITY(1,1),       -- 新闻ID
                      Title NVARCHAR(200) NOT NULL,               -- 标题
                      Content NVARCHAR(MAX),                      -- 内容 (支持HTML富文本)
                      PublishDate DATETIME DEFAULT GETDATE(),     -- 发布日期
                      PublisherID INT,                            -- 发布人ID (外键)
                      IsActive BIT DEFAULT 1,                     -- 是否显示
                      CONSTRAINT FK_News_Publisher FOREIGN KEY (PublisherID) REFERENCES Users(UserID)
);
GO

-- =============================================
-- 8. 文件表 (File)
-- 通用的文件存储记录（如新闻附件、补充材料等）
-- =============================================
CREATE TABLE Files (
                       FileID INT PRIMARY KEY IDENTITY(1,1),       -- 文件ID
                       FileName NVARCHAR(100) NOT NULL,            -- 文件原始名称
                       FilePath NVARCHAR(255) NOT NULL,            -- 服务器存储路径
                       UploadTime DATETIME DEFAULT GETDATE(),      -- 上传时间
                       ManuscriptID INT,                          -- 关联稿件ID (可为空，如果是新闻附件则为空)
                       NewsID INT NULL,                            -- 关联新闻ID(可为空，如果是稿件附件则为空)
                       UploaderID INT,                              -- 上传者ID
                       CONSTRAINT FK_Files_News FOREIGN KEY (NewsID) REFERENCES News(NewsID),
                       CONSTRAINT FK_Files_Manuscript FOREIGN KEY (ManuscriptID) REFERENCES Manuscript(ManuscriptID),
                       CONSTRAINT FK_Files_Uploader FOREIGN KEY (UploaderID) REFERENCES Users(UserID)
);
GO

-- =============================================
-- 9. 日志表 (Logs)
-- 记录系统关键操作，用于审计
-- =============================================
CREATE TABLE Logs (
                      LogID INT PRIMARY KEY IDENTITY(1,1),        -- 日志ID
                      OperationTime DATETIME DEFAULT GETDATE(),   -- 操作时间
                      OperatorID INT,                             -- 操作人ID (外键)
                      OperationType NVARCHAR(50),                 -- 操作类型 (如：Login, Submit, Decision)
                      ManuscriptID INT,                           -- 相关稿件ID (可为空)
                      Description NVARCHAR(MAX),                  -- 操作详情描述
                      CONSTRAINT FK_Logs_Operator FOREIGN KEY (OperatorID) REFERENCES Users(UserID)
);
GO

-- 10. 用户权限表 (UserPermissions)
-- 实现针对每个用户的独立权限配置
CREATE TABLE UserPermissions (
                                 UserID INT PRIMARY KEY,                 -- 主键同时作为外键，确保每个用户只有一条记录

    -- 外键约束，关联到 Users 表
                                 CONSTRAINT FK_UserPermissions_UserID
                                     FOREIGN KEY (UserID) REFERENCES Users(UserID),

    -- ===================================
    -- 权限列：对应于任务书中的各个功能模块
    -- BIT 类型：1 表示有权限，0 表示无权限 (NULL 表示未配置，但使用 0/1 更明确)
    -- ===================================

    -- 稿件相关权限
                                 CanSubmitManuscript BIT DEFAULT 0,          -- 提交新稿件
                                 CanViewAllManuscripts BIT DEFAULT 0,        -- 查看所有稿件 (主编/管理员)

    -- 审稿/决策相关权限
                                 CanAssignReviewer BIT DEFAULT 0,            -- 邀请/指派人员
                                 CanViewReviewerIdentity BIT DEFAULT 0,      -- 查看审稿人身份
                                 CanWriteReview BIT DEFAULT 0,               -- 填写审稿意见
                                 CanMakeDecision BIT DEFAULT 0,              -- 做出录用/拒稿决定

    -- 系统/内容相关权限
                                 CanModifySystemConfig BIT DEFAULT 0,        -- 修改系统配置 (系统管理员/超级管理员)
                                 CanTechCheck BIT DEFAULT 0,                 -- 形式审查 (编辑部管理员)
                                 CanPublishNews BIT DEFAULT 0                -- 发布新闻/公告 (编辑部管理员)

    -- 注意：如果未来新增功能，需要手动 ALTER TABLE ADD COLUMN
);
GO