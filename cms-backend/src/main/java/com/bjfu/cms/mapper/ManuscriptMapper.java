package com.bjfu.cms.mapper;

import com.bjfu.cms.entity.Manuscript;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ManuscriptMapper {
    // 插入新稿件
    int insertManuscript(Manuscript manuscript);

    // 作者查询
    List<Manuscript> selectByAuthorId(@Param("authorId") Integer authorId);

    // 获取所有稿件 (主编全览)
    List<Manuscript> selectAllManuscripts(@Param("status") String status);

    // 获取详情
    Manuscript selectById(Integer id);

    // 更新状态
    void updateStatus(@Param("id") Integer id,
                      @Param("status") String status,
                      @Param("subStatus") String subStatus);

    // 指派编辑
    void updateCurrentEditor(@Param("id") Integer id,
                             @Param("editorId") Integer editorId,
                             @Param("subStatus") String subStatus);

    // 终审决策
    void updateFinalDecision(@Param("id") Integer id,
                             @Param("decision") String decision,
                             @Param("status") String status,
                             @Param("subStatus") String subStatus,
                             @Param("decisionTime") String decisionTime,
                             @Param("editorSummary") String editorSummary);

    // 🔥 新增：特殊权限操作（如撤稿、撤销决定）
    void updateManuscriptSpecial(@Param("id") Integer id,
                                 @Param("status") String status,
                                 @Param("subStatus") String subStatus);
}