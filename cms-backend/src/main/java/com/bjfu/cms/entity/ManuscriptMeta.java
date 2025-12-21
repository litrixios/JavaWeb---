package com.bjfu.cms.entity;

import lombok.Data;

@Data
public class ManuscriptMeta {
    private Integer metaId;
    private Integer manuscriptId;
    private String topic;
    private String recommendedReviewers; // JSON 字符串
    private String coverLetterContent;   // 富文本 HTML
}