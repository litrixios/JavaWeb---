package com.bjfu.cms.entity;

import lombok.Data;

@Data
public class UserPermission {
    private Integer userId;

    // 对应 BIT 类型的字段，使用 Boolean true/false
    private Boolean canSubmitManuscript;
    private Boolean canViewAllManuscripts;
    private Boolean canAssignReviewer;
    private Boolean canViewReviewerIdentity;
    private Boolean canWriteReview;
    private Boolean canMakeDecision;
    private Boolean canModifySystemConfig;
    private Boolean canTechCheck;
    private Boolean canPublishNews;
}