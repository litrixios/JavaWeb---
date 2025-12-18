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
    private String status;       // 大状态
    private String subStatus;    // 小状态
    private Date submissionTime;
    private String decision;
    private String decisionReason;
    private Date decisionTime;
    private String editorRecommendation;
    private String editorSummaryReport;
    private Date recommendationDate;
    private Date revisionDeadline;
}

