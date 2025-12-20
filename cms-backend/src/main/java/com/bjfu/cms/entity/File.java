package com.bjfu.cms.entity;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;

@Data
@Schema(description = "文件实体")
public class File {
    private Integer fileId; // 文件ID
    private String fileName; // 原始文件名
    private String filePath; // 服务器存储路径
    private Date uploadTime; // 上传时间
    private Integer manuscriptId; // 关联稿件ID（可为空）
    private Integer newsId; // 关联新闻ID（可为空）
    private Integer uploaderId; // 上传者ID
}