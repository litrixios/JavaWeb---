package com.bjfu.cms.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.Date;

@Data
@Schema(description = "稿件信息实体") // 建议加上，方便后面在Swagger里面测试
public class Manuscript {
    // 对应 SQL Server 的 ManuscriptID (自增主键)
    @Schema(description = "稿件ID", hidden = true) // hidden=true 表示前端不用传
    private Integer manuscriptId;

    // 作者ID (外键)
    @Schema(description = "作者ID", hidden = true)
    private Integer authorId;

    // 当前编辑ID (可为空)
    private Integer currentEditorId;

    private String title;
    private String abstractText; // 对应数据库 Abstract
    private String keywords;

    // 作者列表 (JSON 字符串)
    private String authorList;

    // 项目资助
    private String fundingInfo;

    // 状态: Submitted, With Editor, Under Review...
    private String status;

    private Date submissionTime;
    private String decision;
    private Date decisionTime;
}