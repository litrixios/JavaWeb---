package com.bjfu.cms.entity.dto;

import lombok.Data;

@Data
public class ManuscriptDTO {
    // 稿件基本信息
    private Integer manuscriptId; // 如果是修改草稿，会带上ID
    private String title;
    private String abstractText;
    private String keywords;
    private String authorList;
    private String fundingInfo;

    // 文件路径 (前端上传文件后获得的路径)
    // 根据 SQL Schema 中的 Versions 表定义 [cite: 524]
    private String originalFilePath;   // 原稿路径
    private String anonymousFilePath;  // 匿名稿路径 (可选)
    private String coverLetterPath;    // Cover Letter路径

    // 操作类型: "SAVE" (保存草稿) 或 "SUBMIT" (正式提交)
    private String actionType;
}