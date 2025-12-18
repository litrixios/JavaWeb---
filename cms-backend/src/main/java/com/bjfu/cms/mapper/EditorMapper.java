package com.bjfu.cms.mapper;

import com.bjfu.cms.entity.Manuscript;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface EditorMapper {
    // 1. 获取分配给我的稿件
    List<Manuscript> selectMyManuscripts(@Param("editorId") Integer editorId);

    // 2. 批量插入审稿任务 (Review 表需预先建立)
    void batchInsertReviews(@Param("msId") Integer msId, @Param("rIds") List<Integer> rIds, @Param("deadline") Date deadline);

    // 3. 更新稿件的小状态
    void updateSubStatus(@Param("msId") Integer msId, @Param("sub") String sub);

    // 4. 提交建议给主编
    void updateRecommendation(Manuscript manuscript);
}