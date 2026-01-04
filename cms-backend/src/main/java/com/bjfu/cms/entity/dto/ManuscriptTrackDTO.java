package com.bjfu.cms.entity.dto;

import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.SystemLog;
import lombok.Data;
import java.util.List;
import java.util.Map; // 新增导入

@Data
public class ManuscriptTrackDTO {
    // 稿件基本信息（标题、当前状态等）
    private Manuscript manuscript;

    // 历史操作日志列表
    private List<SystemLog> historyLogs;

    // 预计审稿周期
    private String estimatedCycle;

    // 审稿意见列表 (列表中的Map包含：reviewerAlias, comments, suggestion等)
    private List<Map<String, Object>> reviewOpinions;
}