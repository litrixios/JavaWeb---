package com.bjfu.cms.entity.dto;

import lombok.Data;

/**
 * 用于接收主编的操作请求参数
 */
@Data
public class EicDecisionDTO {
    private Integer manuscriptId; // 稿件ID

    // 用于初审和终审
    private String decision;      // 决策动作: "DeskReject", "DeskAccept", "Accept", "Reject", "Revise"
    private String comments;      // 决策意见/理由

    // 用于分配编辑
    private Integer editorId;     // 被指派的编辑ID
    private String fullName;          // 必须有这个
    private String researchDirection; // 必须有这个
    private String email;
    private String username;
    private String affiliation;   // 必须有这个

    // 新增：撤稿操作用户信息
    private Integer operatorId;    // 操作用户ID
    private String operatorName;   // 操作用户姓名
}