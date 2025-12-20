package com.bjfu.cms.entity;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;

@Data
@Schema(description = "新闻实体")
public class News {
    private Integer newsId;
    private String title;
    private String content; // 支持HTML富文本
    private Date publishDate;
    private Integer publisherId;
    private Boolean isActive;
    private Date scheduledPublishDate; // 定时发布时间
}