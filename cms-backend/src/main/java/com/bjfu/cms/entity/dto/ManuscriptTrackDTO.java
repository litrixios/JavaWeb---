package com.bjfu.cms.entity.dto;

import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.SystemLog;
import lombok.Data;
import java.util.List;

@Data
public class ManuscriptTrackDTO {
    // 稿件基本信息（标题、当前状态等）
    private Manuscript manuscript;

    // 历史操作日志列表
    private List<SystemLog> historyLogs;

    // 预计审稿周期（可以是固定文本，也可以根据逻辑计算）
    private String estimatedCycle;
}