package com.bjfu.cms.service;

import com.bjfu.cms.entity.dto.EicDecisionDTO;
import com.bjfu.cms.entity.Manuscript;
import java.util.List;

public interface EditorInChiefService {
    // 全览所有稿件
    List<Manuscript> getAllManuscripts(String statusFilter);

    // 初审 (Desk Review)
    void deskReview(EicDecisionDTO dto);

    // 指派编辑 (Assign Editor)
    void assignEditor(EicDecisionDTO dto);

    // 终审决策 (Final Decision)
    void makeFinalDecision(EicDecisionDTO dto);

    // === 管理审稿人库 ===
    // 只保留这一个，删掉那个带三个String参数的
    void inviteReviewer(EicDecisionDTO dto);
    void auditReviewer(Integer userId, Integer status);
    void removeReviewer(Integer userId, String reason);

    // === 特殊权限 ===
    void retractManuscript(EicDecisionDTO dto);
    void rescindDecision(Integer manuscriptId, String newStatus, String reason);
}