package com.bjfu.cms.entity.dto;

import lombok.Data;
import java.util.List;

@Data
public class ManuscriptDTO {
    // === 1. 对应 Manuscript 原表字段 ===
    private Integer manuscriptId;
    private String actionType;    // "SUBMIT" | "SAVE"
    private String title;
    private String abstractText;
    private String keywords;
    private String fundingInfo;

    // 我们约定：authors 列表在 Service 层会被转成 JSON 字符串，存入 Manuscript 表原本的 AuthorList 字段中
    private List<AuthorItem> authors;

    // === 2. 对应 ManuscriptMeta 扩展表字段 ===
    private String topic;

    private List<ReviewerRecommend> recommendedReviewers;

    private String coverLetterContent;

    // === 3. 对应 Versions 表或文件系统 ===
    private String originalFilePath;
    private String anonymousFilePath;
    private String markedFilePath;
    private String coverLetterPath;
    private String responseLetterPath;

    // --- 内部类 ---
    @Data
    public static class AuthorItem {
        private String name;
        private String email;
        private String unit;
        private String degree;
        private String title;
        private String position;
        private boolean isCorresponding;
    }

    @Data
    public static class ReviewerRecommend {
        private String name;
        private String email;
        private String reason;
    }
}