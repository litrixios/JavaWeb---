-- 1. 站内信表 (InternalMessage) - 您的核心通信存储
CREATE TABLE InternalMessage (
                                 MessageID INT PRIMARY KEY IDENTITY(1,1),

    -- 通信双方 (只存ID，不存邮箱)
                                 SenderID INT,                            -- 发送者ID (0或NULL表示系统自动发送)
                                 ReceiverID INT NOT NULL,                 -- 接收者ID (必填，否则不知道发给谁)

    -- 核心内容分类 (按您的要求设计)
                                 Topic NVARCHAR(50),                      -- 主题/分类 (如：'稿件状态', '审稿邀请', '系统通知')
                                 Title NVARCHAR(200) NOT NULL,            -- 标题 (邮件的 Subject)
                                 Content NVARCHAR(MAX),                   -- 正文 (邮件的 Body，支持HTML)

    -- 状态字段
                                 IsRead BIT DEFAULT 0,                    -- 站内信是否已读
                                 SendTime DATETIME DEFAULT GETDATE(),     -- 产生时间

    -- 外键约束
                                 CONSTRAINT FK_Message_Receiver FOREIGN KEY (ReceiverID) REFERENCES Users(UserID)
);
GO

-- 2. 系统配置表 (SystemConfig) - 仅存储【系统公共邮箱】的配置
-- 如果之前已建，确保有数据即可
BEGIN
CREATE TABLE SystemConfig (
                              ConfigKey NVARCHAR(50) PRIMARY KEY,
                              ConfigValue NVARCHAR(500) NOT NULL
);

-- 初始化系统邮箱 (这里填你自己的QQ/163授权码)
INSERT INTO SystemConfig (ConfigKey, ConfigValue) VALUES
                                                      ('mail_host', 'smtp.163.com'),
                                                      ('mail_port', '465'),
                                                      ('mail_username', 'litaihe2@163.com'), -- 系统的公用发件地址
                                                      ('mail_password', 'VMetRp2N8tV5Q94A');       -- 系统的授权码
END
GO

-- =============================================
-- 新增：稿件扩展信息表 (ManuscriptMeta)
-- 用于存放不方便修改主表时的新增字段
-- =============================================
CREATE TABLE ManuscriptMeta (
                                MetaID INT PRIMARY KEY IDENTITY(1,1),
                                ManuscriptID INT NOT NULL UNIQUE,           -- 关联主表ID
                                Topic NVARCHAR(200),                        -- [新增] 研究主题
                                RecommendedReviewers NVARCHAR(MAX),         -- [新增] 推荐审稿人 (存JSON字符串)
                                CoverLetterContent NVARCHAR(MAX),           -- [新增] Cover Letter 富文本内容 (HTML)

    -- 建立外键约束，确保数据一致性
                                CONSTRAINT FK_ManuscriptMeta_Manuscript FOREIGN KEY (ManuscriptID) REFERENCES Manuscript(ManuscriptID) ON DELETE CASCADE
);
GO