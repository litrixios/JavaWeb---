package com.bjfu.cms.entity;

import java.util.Date;

public class Log {
    private Integer logId;
    private Date operationTime;
    private Integer operatorId;
    private String operatorName; // 用户名
    private String operatorFullName; // 新增：用户全名
    private String operationType;
    private Integer manuscriptId;
    private String description;

    // 构造器
    public Log() {}

    public Log(Integer logId, Date operationTime, Integer operatorId, String operatorName,
               String operatorFullName, String operationType, Integer manuscriptId, String description) {
        this.logId = logId;
        this.operationTime = operationTime;
        this.operatorId = operatorId;
        this.operatorName = operatorName;
        this.operatorFullName = operatorFullName;
        this.operationType = operationType;
        this.manuscriptId = manuscriptId;
        this.description = description;
    }

    // getter和setter方法
    public Integer getLogId() { return logId; }
    public void setLogId(Integer logId) { this.logId = logId; }

    public Date getOperationTime() { return operationTime; }
    public void setOperationTime(Date operationTime) { this.operationTime = operationTime; }

    public Integer getOperatorId() { return operatorId; }
    public void setOperatorId(Integer operatorId) { this.operatorId = operatorId; }

    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }

    public String getOperatorFullName() { return operatorFullName; }
    public void setOperatorFullName(String operatorFullName) { this.operatorFullName = operatorFullName; }

    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }

    public Integer getManuscriptId() { return manuscriptId; }
    public void setManuscriptId(Integer manuscriptId) { this.manuscriptId = manuscriptId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}