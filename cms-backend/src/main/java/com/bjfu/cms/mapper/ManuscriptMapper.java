package com.bjfu.cms.mapper;

import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.SystemLog;
import com.bjfu.cms.entity.SystemLog;
import com.bjfu.cms.entity.dto.ReviewTrackingDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
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


    // 根据主键查询
    Manuscript selectByPrimaryKey(@Param("manuscriptId") Integer manuscriptId);

    // 更新状态
    void updateStatus(@Param("manuscriptId") Integer manuscriptId,
                      @Param("status") String status,
                      @Param("subStatus") String subStatus);

    // 更新最终决定
    void updateFinalDecision(@Param("manuscriptId") Integer manuscriptId,
                             @Param("decision") String decision,
                             @Param("status") String status,
                             @Param("subStatus") String subStatus,
                             @Param("finalDecisionDate") String finalDecisionDate,
                             @Param("comments") String comments);

    // 更新当前编辑
    void updateCurrentEditor(@Param("manuscriptId") Integer manuscriptId,
                             @Param("editorId") Integer editorId,
                             @Param("subStatus") String subStatus);

    // 更新编辑和状态
    void updateCurrentEditorAndStatus(@Param("manuscriptId") Integer manuscriptId,
                                      @Param("editorId") Integer editorId,
                                      @Param("status") String status,
                                      @Param("subStatus") String subStatus);

    // 特殊更新（用于撤稿等）
    void updateManuscriptSpecial(@Param("manuscriptId") Integer manuscriptId,
                                 @Param("status") String status,
                                 @Param("subStatus") String subStatus);

    // 新增：更新撤稿信息的专门方法
    void updateRetractManuscript(@Param("manuscriptId") Integer manuscriptId,
                                 @Param("status") String status,
                                 @Param("subStatus") String subStatus,
                                 @Param("decision") String decision,
                                 @Param("retractTime") Date retractTime,
                                 @Param("retractByUserId") Integer retractByUserId,
                                 @Param("retractReason") String retractReason,
                                 @Param("retractType") String retractType);

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

    // 在 ManuscriptMapper 接口中
    List<ReviewTrackingDTO> selectTrackingWithOpinions(@Param("editorId") Integer editorId);
}