package com.bjfu.cms.entity.dto;

import lombok.Data;

import java.util.List;

// 接收邀请审稿人请求的包装类
@Data
public class ReviewInviteDTO {
    private Integer manuscriptId;
    private List<Integer> reviewerIds; // 选中的审稿人ID列表
}
