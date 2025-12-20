package com.bjfu.cms.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SystemConfigMapper {
    // 简单的根据 Key 查 Value
    @Select("SELECT ConfigValue FROM SystemConfig WHERE ConfigKey = #{key}")
    String getValue(@Param("key") String key);
}