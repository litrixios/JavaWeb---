package com.bjfu.cms.entity.dto;

import java.util.Date;

public class LogQueryDTO {
    private String operationType;
    private Date startDate;
    private Date endDate;
    private Integer pageNum = 1;
    private Integer pageSize = 10;

    // 构造器、getter、setter
    public LogQueryDTO() {}

    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public Integer getPageNum() { return pageNum; }
    public void setPageNum(Integer pageNum) { this.pageNum = pageNum; }

    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
}