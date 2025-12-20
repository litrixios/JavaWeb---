package com.bjfu.cms.entity;

import lombok.Data;
import java.util.Date;

/**
 * 稿件版本实体类
 * 对应数据库表: Versions
 */
@Data
public class Version {
    // 对应 VersionID (INT PRIMARY KEY)
    private Integer versionId;

    // 对应 ManuscriptID (INT)
    private Integer manuscriptId;

    // 对应 VersionNumber (INT)
    private Integer versionNumber;

    // 对应 AnonymousFilePath (NVARCHAR)
    // 匿名版文件路径 (PDF)
    private String anonymousFilePath;

    // 对应 OriginalFilePath (NVARCHAR)
    // 原版文件路径 (PDF)
    private String originalFilePath;

    // 对应 CoverLetterPath (NVARCHAR)
    private String coverLetterPath;

    // 对应 ResponseLetterPath (NVARCHAR)
    // 回复信路径 (修回时使用)
    private String responseLetterPath;

    // 对应 UploadTime (DATETIME)
    private Date uploadTime;
}