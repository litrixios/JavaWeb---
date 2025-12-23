package com.bjfu.cms.mapper;

import com.bjfu.cms.entity.Log;
import com.bjfu.cms.entity.dto.LogQueryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface LogMapper {

    // 查询日志列表（带分页和条件）
    List<Log> selectLogsWithOperator(LogQueryDTO queryDTO);

    // 查询日志总数（用于分页）
    int countLogs(LogQueryDTO queryDTO);

    // 清空所有日志
    int deleteAllLogs();

    // 插入日志
    int insertLog(Log log);
}