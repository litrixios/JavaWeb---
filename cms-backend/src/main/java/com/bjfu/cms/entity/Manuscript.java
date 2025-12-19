package com.bjfu.cms.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "稿件信息实体")
public class Manuscript {
    private Integer manuscriptId;
    private Integer authorId;
    private Integer currentEditorId;
    private String title;
    private String abstractText; // 映射数据库 Abstract
    private String keywords;
    private String authorList;
    private String fundingInfo;

    // 状态: Submitted, With Editor, Under Review...
    private String status;
    private String subStatus; // 必须添加，否则 XML 映射失败
    private String editorRecommendation;
    private String editorSummaryReport; // 必须添加，对应 XML 中的 updateFinalDecision
    private Date recommendationDate;
    private Date submissionTime;
    private String decision;
    private String decisionReason;
    private Date decisionTime;
    private Date revisionDeadline;
}

