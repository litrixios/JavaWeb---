package com.bjfu.cms.entity.dto;

import com.bjfu.cms.entity.UserPermission;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 角色权限更新DTO（用于接收角色默认权限的修改请求）
 */
@Data
@Schema(description = "角色权限配置DTO")
public class RolePermissionDTO {
    // 继承UserPermission中的所有权限字段，保持权限定义一致性
    @Schema(description = "是否允许提交稿件")
    private Boolean canSubmitManuscript;

    @Schema(description = "是否允许查看所有稿件")
    private Boolean canViewAllManuscripts;

    @Schema(description = "是否允许分配审稿人")
    private Boolean canAssignReviewer;

    @Schema(description = "是否允许查看审稿人身份")
    private Boolean canViewReviewerIdentity;

    @Schema(description = "是否允许撰写审稿意见")
    private Boolean canWriteReview;

    @Schema(description = "是否允许做出决策")
    private Boolean canMakeDecision;

    @Schema(description = "是否允许修改系统配置")
    private Boolean canModifySystemConfig;

    @Schema(description = "是否允许技术审查")
    private Boolean canTechCheck;

    @Schema(description = "是否允许发布新闻")
    private Boolean canPublishNews;

    /**
     * 转换为UserPermission实体（用于权限存储）
     */
    public UserPermission toUserPermission(Integer userId) {
        UserPermission permission = new UserPermission();
        permission.setUserId(userId);
        permission.setCanSubmitManuscript(this.canSubmitManuscript);
        permission.setCanViewAllManuscripts(this.canViewAllManuscripts);
        permission.setCanAssignReviewer(this.canAssignReviewer);
        permission.setCanViewReviewerIdentity(this.canViewReviewerIdentity);
        permission.setCanWriteReview(this.canWriteReview);
        permission.setCanMakeDecision(this.canMakeDecision);
        permission.setCanModifySystemConfig(this.canModifySystemConfig);
        permission.setCanTechCheck(this.canTechCheck);
        permission.setCanPublishNews(this.canPublishNews);
        return permission;
    }
}