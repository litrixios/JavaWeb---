package com.bjfu.cms.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "编委成员信息实体")
public class EditorialBoard {
    @Schema(description = "成员ID")
    private Integer boardId;

    @Schema(description = "关联的用户ID")
    private Integer userId;

    @Schema(description = "职位 (如: Editor-in-Chief)")
    private String position;

    @Schema(description = "个人简介")
    private String introduction;

    @Schema(description = "所属栏目/领域")
    private String section;

    @Schema(description = "是否在前台显示 (1:显示, 0:隐藏)")
    private Boolean isVisible;
}