package com.bjfu.cms.entity.dto;

import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.SystemLog;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ManuscriptDetailDTO {
    // 稿件基本信息
    private Manuscript manuscript;

    // 历史操作日志（包含提交、分配、决策记录）
    private List<SystemLog> historyLogs;

    // 审稿人意见摘要 (列表中的Map包含：reviewerName, score, advice, comments)
    private List<Map<String, Object>> reviewSummaries;
}