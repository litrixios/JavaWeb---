package com.bjfu.cms.entity;

import lombok.Data;
import java.util.Date;

@Data
public class SystemLog {
    private Integer logId;
    private Date operationTime;
    private Integer operatorId;
    private String operationType; // 如: "SUBMIT", "ASSIGN", "REVIEW"
    private Integer manuscriptId;
    private String description;

    // 扩展字段，用于展示操作人名字（非数据库原生字段）
    private String operatorName;
}