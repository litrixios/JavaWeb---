package com.bjfu.cms.entity.dto;

import lombok.Data;

@Data
public class ReviewInvitationResponseDTO {
    private Integer reviewId;       // 审稿任务ID
    private boolean isAccepted;     // 是否接受
    private String reason;          // 拒绝理由（拒绝时必填）
}