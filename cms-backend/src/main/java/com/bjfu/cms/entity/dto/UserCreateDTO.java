package com.bjfu.cms.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "创建用户DTO")
public class UserCreateDTO {
    @Schema(description = "用户名", required = true)
    private String username;

    @Schema(description = "密码", required = true)
    private String password;

    @Schema(description = "角色", required = true)
    private String role;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "姓名")
    private String fullName;

    @Schema(description = "所属单位")
    private String affiliation;

    @Schema(description = "研究方向")
    private String researchDirection;

    @Schema(description = "状态")
    private Integer status = 0;
}