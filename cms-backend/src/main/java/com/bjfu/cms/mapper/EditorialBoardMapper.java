package com.bjfu.cms.mapper;

import com.bjfu.cms.entity.EditorialBoard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface EditorialBoardMapper {
    // 【前台】只查可见的成员，并关联 Users 表取 FullName 和 AvatarUrl
    List<Map<String, Object>> selectVisibleForPublic();

    // 【后台】查询所有成员（含隐藏的），用于管理员管理
    List<Map<String, Object>> selectAllForAdmin();

    // 基础 CRUD
    int insert(EditorialBoard board);
    int update(EditorialBoard board);
    int delete(Integer boardId);

    // 专门用于切换显隐状态的轻量级更新
    int updateVisibility(@Param("boardId") Integer boardId, @Param("isVisible") Boolean isVisible);
}