package com.bjfu.cms.entity.dto;

import lombok.Data;

@Data
public class TechCheckAnalysisDTO {
    private Integer manuscriptId;
    private String originalFilePath; // 远程文件路径
    private String localTempPath; // 本地临时路径
    private Integer wordCount; // 字数统计
    private Double plagiarismRate; // 查重率(0-1)
}