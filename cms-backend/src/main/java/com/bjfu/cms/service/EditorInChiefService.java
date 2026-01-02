package com.bjfu.cms.service;

import com.bjfu.cms.entity.dto.EicDecisionDTO;
import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.User;
import java.util.List;
import java.util.Map;

public interface EditorInChiefService {
    // 全览所有稿件
    List<Manuscript> getAllManuscripts(String statusFilter);

    // 根据状态分组统计
    Map<String, Integer> getManuscriptStatistics();

    // 导出报表
    byte[] exportReport(String startDate, String endDate);

    // 初审 (Desk Review)
    void deskReview(EicDecisionDTO dto);

    // 批量初审
    void batchDeskReview(List<EicDecisionDTO> dtos);

    // 指派编辑 (Assign Editor)
    void assignEditor(EicDecisionDTO dto);

    // 获取编辑列表
    List<User> getEditorList();

    // 根据专长筛选编辑
    List<User> getEditorsByExpertise(String expertise);

    // 终审决策 (Final Decision)
    void makeFinalDecision(EicDecisionDTO dto);

    // === 管理审稿人库 ===
    void inviteReviewer(EicDecisionDTO dto);
    void auditReviewer(Integer userId, Integer status);
    void removeReviewer(Integer userId, String reason);

    // 获取审稿人列表
    List<User> getReviewerList();

    // === 特殊权限 ===
    void retractManuscript(EicDecisionDTO dto);
    void rescindDecision(Integer manuscriptId, String newStatus, String reason);
}
