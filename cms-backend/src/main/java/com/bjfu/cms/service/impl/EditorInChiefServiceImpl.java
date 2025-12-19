package com.bjfu.cms.service.impl;

import com.bjfu.cms.common.utils.UserContext;
import com.bjfu.cms.entity.User;
import com.bjfu.cms.entity.dto.EicDecisionDTO;
import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.mapper.ManuscriptMapper;
import com.bjfu.cms.mapper.UserMapper;
import com.bjfu.cms.service.EditorInChiefService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Service
public class EditorInChiefServiceImpl implements EditorInChiefService {

    @Autowired
    private ManuscriptMapper manuscriptMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<Manuscript> getAllManuscripts(String statusFilter) {
        return manuscriptMapper.selectAllManuscripts(statusFilter);
    }

    @Override
    @Transactional
    public void deskReview(EicDecisionDTO dto) {
        if ("DeskReject".equals(dto.getDecision())) {
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            manuscriptMapper.updateFinalDecision(dto.getManuscriptId(), "Reject", "Decided", "Rejected", now, dto.getComments());
        } else if ("DeskAccept".equals(dto.getDecision())) {
            manuscriptMapper.updateStatus(dto.getManuscriptId(), "Processing", "PendingAssign");
        }
    }

    @Override
    @Transactional
    public void assignEditor(EicDecisionDTO dto) {
        if (dto.getEditorId() == null) throw new RuntimeException("必须选择一位编辑");
        manuscriptMapper.updateCurrentEditor(dto.getManuscriptId(), dto.getEditorId(), "WithEditor");
    }

    @Override
    @Transactional
    public void makeFinalDecision(EicDecisionDTO dto) {
        String status = "Accept".equalsIgnoreCase(dto.getDecision()) ? "Decided" : ("Revise".equalsIgnoreCase(dto.getDecision()) ? "Revision" : "Decided");
        String subStatus = "Accept".equalsIgnoreCase(dto.getDecision()) ? "Accepted" : ("Revise".equalsIgnoreCase(dto.getDecision()) ? "NeedRevision" : "Rejected");
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        manuscriptMapper.updateFinalDecision(dto.getManuscriptId(), dto.getDecision(), status, subStatus, now, dto.getComments());
    }

    // === (5) 管理审稿人库：实现邀请审稿人 ===
    @Override
    @Transactional
    public void inviteReviewer(EicDecisionDTO dto) {
        // 检查必要参数
        if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
            throw new RuntimeException("邀请审稿人必须提供邮箱");
        }

        User newUser = new User();
        // 示范操作：输入姓名“Wang Wu”、邮箱“wangwu@example.com”、领域“机器学习”
        newUser.setUsername(dto.getEmail()); // 账号名默认用邮箱
        newUser.setEmail(dto.getEmail());
        //newUser.setFullName(dto.getComments()); // 对应示范中的 姓名
        //newUser.setResearchDirection(dto.getDecision()); // 对应示范中的 领域
        newUser.setFullName(dto.getFullName());             // 对应 JSON 的 "fullName"
        newUser.setResearchDirection(dto.getResearchDirection()); // 对应 JSON 的 "researchDirection"
        newUser.setRole("Reviewer");
        newUser.setStatus(0); // 待审核状态
        newUser.setRegisterTime(new Date());

        userMapper.insertUser(newUser);

        // 记录日志，对应任务书“确保审计”的要求
        saveLog("Invite", null, "邀请审稿人: " + dto.getEmail() + " (" + dto.getComments() + ")");
    }

    @Override
    @Transactional
    public void auditReviewer(Integer userId, Integer status) {
        userMapper.updateUserStatus(userId, status);
        saveLog("Audit", null, "审核审稿人ID: " + userId + " 结果状态: " + status);
    }

    @Override
    @Transactional
    public void removeReviewer(Integer userId, String reason) {
        // 逻辑移除：状态设为2（黑名单/注销）
        userMapper.updateUserStatus(userId, 2);
        saveLog("Remove", null, "移除审稿人ID: " + userId + " 原因: " + reason);
    }

    @Override
    @Transactional
    public void retractManuscript(EicDecisionDTO dto) {
        // 将状态改为数据库约束允许的 'Decided' 和 'Rejected'
        manuscriptMapper.updateManuscriptSpecial(dto.getManuscriptId(), "Decided", "Rejected");
        saveLog("Retract", dto.getManuscriptId(), "撤稿原因: " + dto.getComments());
    }

    @Override
    @Transactional
    public void rescindDecision(Integer manuscriptId, String newStatus, String reason) {
        // 撤销决定通常是将状态改回上一阶段
        manuscriptMapper.updateManuscriptSpecial(manuscriptId, "Processing", newStatus);
        saveLog("Rescind", manuscriptId, "撤销决定原因: " + reason);
    }

    private void saveLog(String type, Integer manuscriptId, String desc) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        userMapper.insertLog(now, UserContext.getUserId(), type, manuscriptId, desc);
    }
}