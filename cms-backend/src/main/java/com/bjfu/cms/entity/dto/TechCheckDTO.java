package com.bjfu.cms.entity.dto;

import lombok.Data;

@Data
public class TechCheckDTO {
    private Integer manuscriptId;
    private Boolean passed; // true表示通过，false表示不通过
    private String feedback; // 审查反馈
    private Integer wordCount; // 字数
    private String formatIssues; // 格式问题
    private Double plagiarismRate; // 查重率
}