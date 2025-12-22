package com.bjfu.cms.entity.dto;

import lombok.Data;

@Data
public class ReviewSubmitDTO {
    private Integer reviewId;
    private String confidentialComments; // 给编辑的保密意见
    private String authorComments;       // 给作者的公开意见
    private Integer innovationScore;     // 创新性评分 (1-5)
    private Integer methodologyScore;    // 方法论评分 (1-5)
    private Integer qualityScore;        // 整体质量评分 (1-5)
    private String recommendation;       // 总体建议: Accept/Minor Revision/Major Revision/Reject
}