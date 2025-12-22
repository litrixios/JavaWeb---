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

//        // 先查询当前稿件状态，确保符合状态流转规则
//        Manuscript manuscript = manuscriptMapper.selectByPrimaryKey(dto.getManuscriptId());
//        if (manuscript == null) {
//            throw new RuntimeException("稿件不存在");
//        }
//
//        // 检查当前状态是否允许指派编辑
//        if (!"Processing".equals(manuscript.getStatus()) || !"PendingAssign".equals(manuscript.getSubStatus())) {
//            throw new RuntimeException("当前稿件状态不允许指派编辑");
//        }

        // 更新当前编辑ID和状态，确保符合约束
        // Status 保持为 'Processing'，SubStatus 更新为 'WithEditor'
        manuscriptMapper.updateCurrentEditorAndStatus(dto.getManuscriptId(), dto.getEditorId(), "Processing", "WithEditor");

        // 记录日志
        saveLog("AssignEditor", dto.getManuscriptId(), "指派编辑ID: " + dto.getEditorId());
    }

    @Override
    @Transactional
    public void makeFinalDecision(EicDecisionDTO dto) {
        // 严格匹配：Status='Decided' 配对 SubStatus='Accepted' 或 'Rejected'
        String status = "Decided";
        String subStatus = "Accept".equalsIgnoreCase(dto.getDecision()) ? "Accepted" : "Rejected";

        // 如果是修改建议，Status='Revision', SubStatus 必须为 null
        if ("Revise".equalsIgnoreCase(dto.getDecision())) {
            status = "Revision";
            subStatus = null;
        }

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