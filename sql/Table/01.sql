-- =============================================
-- 1. �û��� (Users)
-- ������������Ա���༭�����ߡ�����˵����н�ɫ
-- =============================================
CREATE TABLE Users (
                       UserID INT PRIMARY KEY IDENTITY(1,1),      -- �û�ID������
                       Username NVARCHAR(50) NOT NULL UNIQUE,     -- �û�����Ψһ
                       Password NVARCHAR(100) NOT NULL,           -- ���� (�������ܺ�Ĺ�ϣֵ��������ڿ�������)
                       Role NVARCHAR(20) NOT NULL,                -- ��ɫ���� (SuperAdmin, Admin, Author, Reviewer, Editor, EditorInChief, EditorialAdmin)
                       Email NVARCHAR(100) NOT NULL,              -- ����
                       FullName NVARCHAR(50),                     -- ȫ�� (��ʾ��ҳ�����Ͻ�)
                       Affiliation NVARCHAR(100),                 -- ��λ
                       ResearchDirection NVARCHAR(200),           -- �о�����
                       RegisterTime DATETIME DEFAULT GETDATE(),   -- ע��ʱ��
                       Status INT DEFAULT 0,                       -- ״̬ (0: �����/δ����, 1: ����, 2: ����)
                       AvatarUrl NVARCHAR(500) NULL                 -- �������洢����ͷ���ͼƬ·��
);
GO

-- =============================================
-- 2. �ڿ��� (Journal)
-- �洢�ڿ��Ļ�����Ϣ
-- =============================================
CREATE TABLE Journal (
                         JournalID INT PRIMARY KEY IDENTITY(1,1),   -- �ڿ�ID
                         Name NVARCHAR(100) NOT NULL,               -- �ڿ����� (�磺International Artificial Intelligence Research)
                         Introduction NVARCHAR(MAX),                -- �ڿ�����
                         ImpactFactor DECIMAL(6, 3),                -- Ӱ������ (����: 3.502)
                         Timeline NVARCHAR(MAX),                    -- ����ʱ����˵��
                         JournalLink NVARCHAR(255),                 -- �������������ԣ�����ǰ����ת
                         JournalImageLink NVARCHAR(666)             -- �洢�ڿ������ͼƬ URL ����

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
-- 4. ����汾�� (Versions)
-- ����֧���޻ع��ܣ��洢����Ĳ�ͬ�汾
-- =============================================
CREATE TABLE Versions (
                          VersionID INT PRIMARY KEY IDENTITY(1,1),    -- �汾ID
                          ManuscriptID INT NOT NULL,                  -- ���ID (���)
                          VersionNumber INT NOT NULL,                 -- �汾�� (1, 2, 3...)
                          AnonymousFilePath NVARCHAR(255),            -- �������ļ�·�� (PDF)
                          OriginalFilePath NVARCHAR(255),             -- ԭ���ļ�·�� (PDF)
                          CoverLetterPath NVARCHAR(255),              -- Cover Letter·��
                          MarkerFilePath NVARCHAR(255),
                          ResponseLetterPath NVARCHAR(255),           -- �ظ���·�� (�޻�ʱʹ��)
                          UploadTime DATETIME DEFAULT GETDATE(),      -- �ϴ�ʱ��
                          CONSTRAINT FK_Versions_Manuscript FOREIGN KEY (ManuscriptID) REFERENCES Manuscript(ManuscriptID)
);
GO

-- =============================================
-- 5. �������� (Review)
-- �洢����������Ĺ����������
-- =============================================
CREATE TABLE Review (
                        ReviewID INT PRIMARY KEY IDENTITY(1,1),     -- ����ID
                        ManuscriptID INT NOT NULL,                  -- ���ID (���)
                        ReviewerID INT NOT NULL,                    -- �����ID (���)
                        CommentsToAuthor NVARCHAR(MAX),             -- �����ߵĹ������
                        ConfidentialComments NVARCHAR(MAX),         -- ���༭�ı������
                        Score INT,                                  -- ��� (���� 1-5 ��)
                        Suggestion NVARCHAR(50),                    -- ���� (Accept, Minor Revision, Major Revision, Reject)
                        InviteDate DATETIME DEFAULT GETDATE(),      -- ��������
                        Deadline DATETIME,                          -- ��ֹ����
                        SubmitTime DATETIME,                        -- ����ύʱ��
                        Status NVARCHAR(20) DEFAULT 'Invited',      -- ״̬ (Invited, Accepted, Rejected, Completed, Overdue)
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
-- 7. ���ű� (News)
-- ����ǰ̨չʾ֪ͨ�͹���
-- =============================================
CREATE TABLE News (
                      NewsID INT PRIMARY KEY IDENTITY(1,1),       -- ����ID
                      Title NVARCHAR(200) NOT NULL,               -- ����
                      Content NVARCHAR(MAX),                      -- ���� (֧��HTML���ı�)
                      PublishDate DATETIME DEFAULT GETDATE(),     -- ��������
                      PublisherID INT,                            -- ������ID (���)
                      IsActive BIT DEFAULT 1,                     -- �Ƿ���ʾ
                      CONSTRAINT FK_News_Publisher FOREIGN KEY (PublisherID) REFERENCES Users(UserID)
);
GO

-- =============================================
-- 8. �ļ��� (File)
-- ͨ�õ��ļ��洢��¼�������Ÿ�����������ϵȣ�
-- =============================================
CREATE TABLE Files (
                       FileID INT PRIMARY KEY IDENTITY(1,1),       -- �ļ�ID
                       FileName NVARCHAR(100) NOT NULL,            -- �ļ�ԭʼ����
                       FilePath NVARCHAR(255) NOT NULL,            -- �������洢·��
                       UploadTime DATETIME DEFAULT GETDATE(),      -- �ϴ�ʱ��
                       ManuscriptID INT,                          -- �������ID (��Ϊ�գ���������Ÿ�����Ϊ��)
                       NewsID INT NULL,                            -- ��������ID(��Ϊ�գ�����Ǹ��������Ϊ��)
                       UploaderID INT,                              -- �ϴ���ID
                       CONSTRAINT FK_Files_News FOREIGN KEY (NewsID) REFERENCES News(NewsID),
                       CONSTRAINT FK_Files_Manuscript FOREIGN KEY (ManuscriptID) REFERENCES Manuscript(ManuscriptID),
                       CONSTRAINT FK_Files_Uploader FOREIGN KEY (UploaderID) REFERENCES Users(UserID)
);
GO

-- =============================================
-- 9. ��־�� (Logs)
-- ��¼ϵͳ�ؼ��������������
-- =============================================
CREATE TABLE Logs (
                      LogID INT PRIMARY KEY IDENTITY(1,1),        -- ��־ID
                      OperationTime DATETIME DEFAULT GETDATE(),   -- ����ʱ��
                      OperatorID INT,                             -- ������ID (���)
                      OperationType NVARCHAR(50),                 -- �������� (�磺Login, Submit, Decision)
                      ManuscriptID INT,                           -- ��ظ��ID (��Ϊ��)
                      Description NVARCHAR(MAX),                  -- ������������
                      CONSTRAINT FK_Logs_Operator FOREIGN KEY (OperatorID) REFERENCES Users(UserID)
);
GO

-- 10. �û�Ȩ�ޱ� (UserPermissions)
-- ʵ�����ÿ���û��Ķ���Ȩ������
CREATE TABLE UserPermissions (
                                 UserID INT PRIMARY KEY,                 -- ����ͬʱ��Ϊ�����ȷ��ÿ���û�ֻ��һ����¼

    -- ���Լ���������� Users ��
                                 CONSTRAINT FK_UserPermissions_UserID
                                     FOREIGN KEY (UserID) REFERENCES Users(UserID),

    -- ===================================
    -- Ȩ���У���Ӧ���������еĸ�������ģ��
    -- BIT ���ͣ�1 ��ʾ��Ȩ�ޣ�0 ��ʾ��Ȩ�� (NULL ��ʾδ���ã���ʹ�� 0/1 ����ȷ)
    -- ===================================

    -- ������Ȩ��
                                 CanSubmitManuscript BIT DEFAULT 0,          -- �ύ�¸��
                                 CanViewAllManuscripts BIT DEFAULT 0,        -- �鿴���и�� (����/����Ա)

    -- ���/�������Ȩ��
                                 CanAssignReviewer BIT DEFAULT 0,            -- ����/ָ����Ա
                                 CanViewReviewerIdentity BIT DEFAULT 0,      -- �鿴��������
                                 CanWriteReview BIT DEFAULT 0,               -- ��д������
                                 CanMakeDecision BIT DEFAULT 0,              -- ����¼��/�ܸ����

    -- ϵͳ/�������Ȩ��
                                 CanModifySystemConfig BIT DEFAULT 0,        -- �޸�ϵͳ���� (ϵͳ����Ա/��������Ա)
                                 CanTechCheck BIT DEFAULT 0,                 -- ��ʽ��� (�༭������Ա)
                                 CanPublishNews BIT DEFAULT 0                -- ��������/���� (�༭������Ա)

    -- ע�⣺���δ���������ܣ���Ҫ�ֶ� ALTER TABLE ADD COLUMN
);
GO