package com.bjfu.cms.mapper;

import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.SystemLog;
import com.bjfu.cms.entity.Version; // 需新建 Version 实体，或直接用 Map 传参，这里建议新建实体
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ManuscriptMapper {
    // 插入新稿件
    int insertManuscript(Manuscript manuscript);

    // 作者查询
    // 新增：更新稿件信息（用于编辑草稿）
    int updateManuscript(Manuscript manuscript);

    // 新增：插入版本记录 (对应 Versions 表)
    int insertVersion(@Param("manuscriptId") Integer manuscriptId,
                      @Param("versionNumber") Integer versionNumber,
                      @Param("originalFilePath") String originalFilePath,
                      @Param("anonymousFilePath") String anonymousFilePath,
                      @Param("coverLetterPath") String coverLetterPath,
                      @Param("markedFilePath") String markedFilePath,       // 新增
                      @Param("responseLetterPath") String responseLetterPath // 新增
    );

    Integer selectMaxVersion(@Param("manuscriptId") Integer manuscriptId);

    /**
     * 根据数据库字段直接筛选
     * @param authorId 作者ID (null查所有)
     * @param status 大状态 (Incomplete/Processing/Revision/Decided)
     * @param subStatus 小状态 (TechCheck/WithEditor/...)
     */
    List<Manuscript> selectManuscripts(
            @Param("authorId") Integer authorId,
            @Param("status") String status,
            @Param("subStatus") String subStatus
    );

    // 获取所有稿件 (主编全览)
    List<Manuscript> selectAllManuscripts(@Param("status") String status);


    // 更新状态
    void updateStatus(@Param("id") Integer id,
                      @Param("status") String status,
                      @Param("subStatus") String subStatus);

    // 指派编辑
    void updateCurrentEditorAndStatus(@Param("manuscriptId") Integer manuscriptId,
                                      @Param("editorId") Integer editorId,
                                      @Param("status") String status,
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
    // 新增：根据ID查询，方便校验归属
    Manuscript selectById(@Param("id") Integer id);

    // ManuscriptMapper.java 补充
    /**
     * 获取稿件最新版本的匿名文件路径 (AnonymousFilePath)
     */
    String selectLatestAnonymousFilePath(@Param("manuscriptId") Integer manuscriptId);

    // 获取待形式审查的稿件
    List<Manuscript> selectTechCheckManuscripts();
    /**
     * 获取稿件最新版本的OriginalFilePath
     */
    String selectLatestOriginalFilePath(Integer manuscriptId);
    List<SystemLog> selectLogsByManuscriptId(Integer manuscriptId);
    Manuscript selectManuscriptForReview(@Param("manuscriptId") Integer manuscriptId);
}