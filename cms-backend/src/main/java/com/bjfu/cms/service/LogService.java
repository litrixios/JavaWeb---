package com.bjfu.cms.service;

import com.bjfu.cms.entity.Log;
import com.bjfu.cms.entity.dto.LogQueryDTO;
import com.bjfu.cms.mapper.LogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LogService {

    @Autowired
    private LogMapper logMapper;

    public Map<String, Object> getLogs(LogQueryDTO queryDTO) {
        Map<String, Object> result = new HashMap<>();

        List<Log> logs = logMapper.selectLogsWithOperator(queryDTO);
        int total = logMapper.countLogs(queryDTO);

        result.put("list", logs);
        result.put("total", total);
        result.put("pageNum", queryDTO.getPageNum());
        result.put("pageSize", queryDTO.getPageSize());

        return result;
    }

    public boolean clearAllLogs() {
        try {
            int affectedRows = logMapper.deleteAllLogs();
            return affectedRows >= 0; // 删除成功
        } catch (Exception e) {
            return false;
        }
    }

    public void addLog(Integer operatorId, String operationType, Integer manuscriptId, String description) {
        Log log = new Log();
        log.setOperatorId(operatorId);
        log.setOperationType(operationType);
        log.setManuscriptId(manuscriptId);
        log.setDescription(description);

        logMapper.insertLog(log);
    }
}