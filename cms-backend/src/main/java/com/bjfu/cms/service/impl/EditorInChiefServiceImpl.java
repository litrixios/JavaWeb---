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
import java.util.*;
import java.util.stream.Collectors;

/**
 * 主编服务实现类
 * 实现EditorInChiefService接口定义的所有业务逻辑
 */
@Service
public class EditorInChiefServiceImpl implements EditorInChiefService {

    @Autowired
    private ManuscriptMapper manuscriptMapper; // 稿件数据访问接口

    @Autowired
    private UserMapper userMapper; // 用户数据访问接口

    /**
     * 全览所有稿件
     * @param statusFilter 状态筛选条件
     * @return 符合条件的稿件列表
     */
    @Override
    public List<Manuscript> getAllManuscripts(String statusFilter) {
        return manuscriptMapper.selectAllManuscripts(statusFilter);
    }

    /**
     * 稿件统计
     * 获取稿件状态统计信息，包括各状态稿件数量和平均审稿天数
     * @return 统计信息Map
     */
    @Override
    public Map<String, Integer> getManuscriptStatistics() {
        Map<String, Integer> stats = new HashMap<>();

        // 获取所有稿件
        List<Manuscript> allManuscripts = manuscriptMapper.selectAllManuscripts(null);

        // 初始化计数器
        int pendingDeskReviewCount = 0;  // 待初审
        int pendingAssignCount = 0;      // 待分配
        int withEditorCount = 0;         // 编辑处理中
        int underReviewCount = 0;        // 正在审稿
        int revisionCount = 0;           // 需要修改
        int decidedCount = 0;            // 已有决定

        // 只统计前端需要的6个状态
        for (Manuscript manuscript : allManuscripts) {
            String status = manuscript.getStatus();
            String subStatus = manuscript.getSubStatus();

            // 1. 待初审：status='Processing' and subStatus='PendingDeskReview'
            if ("Processing".equals(status) && "PendingDeskReview".equals(subStatus)) {
                pendingDeskReviewCount++;
            }
            // 2. 待分配：status='Processing' and subStatus='PendingAssign'
            else if ("Processing".equals(status) && "PendingAssign".equals(subStatus)) {
                pendingAssignCount++;
            }
            // 3. 编辑处理中：status='Processing' and subStatus='WithEditor'
            else if ("Processing".equals(status) && "WithEditor".equals(subStatus)) {
                withEditorCount++;
            }
            // 4. 正在审稿：status='Processing' and subStatus='UnderReview'
            else if ("Processing".equals(status) && "UnderReview".equals(subStatus)) {
                underReviewCount++;
            }
            // 5. 需要修改：status='Revision'
            else if ("Revision".equals(status)) {
                revisionCount++;
            }
            // 6. 已有决定：status='Decided'
            else if ("Decided".equals(status)) {
                decidedCount++;
            }
        }

        // 只放入前端需要的6个状态和平均审稿天数
        stats.put("PendingDeskReview", pendingDeskReviewCount);  // 待初审
        stats.put("PendingAssign", pendingAssignCount);          // 待分配
        stats.put("WithEditor", withEditorCount);                // 编辑处理中
        stats.put("UnderReview", underReviewCount);              // 正在审稿
        stats.put("Revision", revisionCount);                    // 需要修改
        stats.put("Decided", decidedCount);                      // 已有决定

        // 计算平均审稿时间
        if (!allManuscripts.isEmpty()) {
            double avgReviewDays = allManuscripts.stream()
                    .filter(m -> m.getDecisionTime() != null && m.getSubmissionTime() != null)
                    .mapToLong(m -> {
                        long diff = m.getDecisionTime().getTime() - m.getSubmissionTime().getTime();
                        return diff / (1000 * 60 * 60 * 24);
                    })
                    .average()
                    .orElse(0.0);

            stats.put("avgReviewDays", (int) avgReviewDays);
        }

        return stats;
    }

    /**
     * 导出报表
     * 导出指定时间范围的稿件处理报表为Excel文件
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return Excel文件字节数组
     */
    @Override
    public byte[] exportReport(String startDate, String endDate) {
        // 这里实现导出Excel报表的逻辑
        // 可以使用Apache POI或EasyExcel来生成Excel文件
        // 返回字节数组
        return new byte[0];
    }

    /**
     * 初审 (Desk Review)
     * 对新投稿进行初步审查，决定直接拒稿或送审
     * @param dto 初审决策数据传输对象
     */
    @Override
    @Transactional
    public void deskReview(EicDecisionDTO dto) {
        // 参数验证
        if (dto.getManuscriptId() == null) {
            throw new IllegalArgumentException("稿件ID不能为空");
        }

        if (!"DeskReject".equals(dto.getDecision()) &&
                !"DeskAccept".equals(dto.getDecision())) {
            throw new IllegalArgumentException("无效的初审决策类型");
        }

        Manuscript manuscript = manuscriptMapper.selectById(dto.getManuscriptId());
        if (manuscript == null) {
            throw new RuntimeException("稿件不存在，ID: " + dto.getManuscriptId());
        }

        try {
            if ("DeskReject".equals(dto.getDecision())) {
                // 初审拒绝：直接终审拒稿
                String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                manuscriptMapper.updateFinalDecision(
                        dto.getManuscriptId(),
                        "Reject",
                        "Decided",
                        "Rejected",
                        now,
                        dto.getComments()
                );

                saveLog("DeskReject", dto.getManuscriptId(),
                        "初审拒绝，理由: " + dto.getComments());
            } else if ("DeskAccept".equals(dto.getDecision())) {
                // 初审通过：进入指派编辑状态
                manuscriptMapper.updateStatus(
                        dto.getManuscriptId(),
                        "Processing",
                        "PendingAssign"  // 改为 PendingAssign，而不是 UnderReview
                );

                saveLog("DeskAccept", dto.getManuscriptId(),
                        "稿件通过初审，等待指派编辑" +
                                (dto.getComments() != null ? "，备注：" + dto.getComments() : ""));
            }
        } catch (Exception e) {
            saveLog("DeskReviewError", dto.getManuscriptId(),
                    "初审失败: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 批量初审
     * 批量进行初审操作，提高处理效率
     * @param dtos 初审决策数据传输对象列表
     */
    @Override
    @Transactional
    public void batchDeskReview(List<EicDecisionDTO> dtos) {
        for (EicDecisionDTO dto : dtos) {
            deskReview(dto);
        }
    }

    /**
     * 指派编辑 (Assign Editor)
     * 将通过初审的稿件分配给特定编辑处理
     * @param dto 指派编辑数据传输对象
     */
    @Override
    @Transactional
    public void assignEditor(EicDecisionDTO dto) {
        if (dto.getEditorId() == null) throw new RuntimeException("必须选择一位编辑");

        // 获取稿件信息
        Manuscript manuscript = manuscriptMapper.selectById(dto.getManuscriptId());
        if (manuscript == null) {
            throw new RuntimeException("稿件不存在");
        }

        // 检查稿件状态是否允许指派编辑
        // 状态应为 Processing，子状态应为 PendingAssign
        if (!"Processing".equals(manuscript.getStatus()) ||
                !"PendingAssign".equals(manuscript.getSubStatus())) {
            throw new RuntimeException("当前稿件状态不允许指派编辑，当前状态：" +
                    manuscript.getStatus() + "/" + manuscript.getSubStatus());
        }

        // 指派编辑后，状态变为 WithEditor
        manuscriptMapper.updateCurrentEditorAndStatus(dto.getManuscriptId(),
                dto.getEditorId(), "Processing", "WithEditor");

        // 记录分配理由
        String reason = dto.getComments() != null ? dto.getComments() : "根据编辑专长匹配";
        saveLog("AssignEditor", dto.getManuscriptId(),
                "指派编辑ID: " + dto.getEditorId() + "，理由: " + reason);
    }

    /**
     * 获取编辑列表
     * 获取所有可指派的编辑列表
     * @return 编辑列表
     */
    @Override
    public List<User> getEditorList() {
        // 查询所有编辑（角色为Editor或AssociateEditor）
        return userMapper.selectEditors();
    }

    /**
     * 根据专长筛选编辑
     * 根据研究方向筛选合适的编辑
     * @param expertise 研究方向/专长
     * @return 符合条件的编辑列表
     */
    @Override
    public List<User> getEditorsByExpertise(String expertise) {
        List<User> allEditors = getEditorList();

        // 根据专长筛选
        return allEditors.stream()
                .filter(editor -> editor.getResearchDirection() != null &&
                        editor.getResearchDirection().toLowerCase().contains(expertise.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * 终审决策 (Final Decision)
     * 基于编辑建议和审稿意见，做出最终录用、拒稿或修改决定
     * @param dto 终审决策数据传输对象
     */
    @Override
    @Transactional
    public void makeFinalDecision(EicDecisionDTO dto) {
        String status = "Decided";
        String subStatus = "Accept".equalsIgnoreCase(dto.getDecision()) ? "Accepted" : "Rejected";

        if ("Revise".equalsIgnoreCase(dto.getDecision())) {
            status = "Revision";
            subStatus = null;
        }

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        manuscriptMapper.updateFinalDecision(dto.getManuscriptId(), dto.getDecision(),
                status, subStatus, now, dto.getComments());

        // 记录终审决策
        saveLog("FinalDecision", dto.getManuscriptId(),
                "终审决策: " + dto.getDecision() + "，理由: " + dto.getComments());
    }

    /**
     * 邀请审稿人
     * 向系统添加新的审稿人账号
     * @param dto 审稿人邀请数据传输对象
     */
    @Override
    @Transactional
    public void inviteReviewer(EicDecisionDTO dto) {
        // 检查必要参数
        if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
            throw new RuntimeException("邀请审稿人必须提供邮箱");
        }

        User newUser = new User();
        newUser.setUsername(dto.getUsername());
        newUser.setEmail(dto.getEmail());
        newUser.setFullName(dto.getFullName());
        newUser.setAffiliation(dto.getAffiliation());
        newUser.setPassword("123456"); // 初始密码
        newUser.setResearchDirection(dto.getResearchDirection());
        newUser.setRole("Reviewer");
        newUser.setStatus(0); // 待审核状态
        newUser.setRegisterTime(new Date());
        userMapper.insertUser(newUser);

        saveLog("InviteReviewer", null,
                "邀请审稿人: " + dto.getFullName() + " (" + dto.getEmail() + ")，领域: " + dto.getResearchDirection());
    }

    /**
     * 获取审稿人列表
     * 获取所有审稿人信息，包括待审核的审稿人
     * @return 审稿人列表
     */
    @Override
    public List<User> getReviewerList() {
        return userMapper.selectByRole("Reviewer");
    }

    /**
     * 审核审稿人
     * 审批或拒绝审稿人加入申请
     * @param userId 用户ID
     * @param status 审核状态（1:通过，0:拒绝）
     */
    @Override
    @Transactional
    public void auditReviewer(Integer userId, Integer status) {
        userMapper.updateUserStatus(userId, status);
        String statusText = status == 1 ? "批准" : "拒绝";
        saveLog("AuditReviewer", null,
                "审核审稿人ID: " + userId + "，结果: " + statusText);
    }

    /**
     * 移除审稿人
     * 将审稿人移出库（加入黑名单）
     * @param userId 用户ID
     * @param reason 移除原因
     */
    @Override
    @Transactional
    public void removeReviewer(Integer userId, String reason) {
        userMapper.updateUserStatus(userId, 2); // 状态设为2（移除/黑名单）
        saveLog("RemoveReviewer", null,
                "移除审稿人ID: " + userId + "，原因: " + reason);
    }

    /**
     * 撤稿
     * 撤回已发布的稿件
     * @param dto 撤稿数据传输对象
     */
    @Override
    @Transactional
    public void retractManuscript(EicDecisionDTO dto) {
        // 获取稿件信息
        Manuscript manuscript = manuscriptMapper.selectById(dto.getManuscriptId());
        if (manuscript == null) {
            throw new RuntimeException("稿件不存在");
        }

        // 从DTO中获取操作者ID，如果为空则尝试从上下文获取
        Integer currentUserId = dto.getOperatorId();
        System.out.println("从DTO获取的operatorId: " + currentUserId);

        if (currentUserId == null) {
            try {
                currentUserId = UserContext.getUserId();
                System.out.println("从UserContext获取的userId: " + currentUserId);
            } catch (Exception e) {
                System.err.println("从UserContext获取用户ID失败: " + e.getMessage());
            }
        }

        // 如果还是为空，则使用默认系统管理员ID
        if (currentUserId == null) {
            currentUserId = 1; // 系统管理员ID
            System.err.println("警告：使用默认系统管理员ID进行撤稿操作");
        }

        System.out.println("最终使用的操作用户ID: " + currentUserId);
        System.out.println("撤稿原因: " + dto.getComments());

        // 获取当前时间
        Date retractTime = new Date();

        try {
            // 更新撤稿信息
            manuscriptMapper.updateRetractManuscript(
                    dto.getManuscriptId(),
                    "Decided",           // Status
                    "Retracted",         // SubStatus
                    "Reject",           // Decision 必须为Reject
                    retractTime,        // RetractTime
                    currentUserId,      // RetractByUserID
                    dto.getComments(),  // RetractReason
                    "Involuntary"       // RetractType
            );

            // 记录日志
            String operatorName = dto.getOperatorName() != null ?
                    dto.getOperatorName() : "未知用户";
            saveLog("Retract", dto.getManuscriptId(),
                    "撤稿原因: " + dto.getComments() +
                            "，操作人: " + operatorName + "(ID:" + currentUserId + ")");

        } catch (Exception e) {
            saveLog("RetractError", dto.getManuscriptId(),
                    "撤稿失败: " + e.getMessage());
            throw new RuntimeException("撤稿失败: " + e.getMessage(), e);
        }
    }

    /**
     * 撤销决定
     * 撤销之前的稿件处理决定，将状态改回上一阶段
     * @param manuscriptId 稿件ID
     * @param newStatus 新的状态
     * @param reason 撤销原因
     */
    @Override
    @Transactional
    public void rescindDecision(Integer manuscriptId, String newStatus, String reason) {
        manuscriptMapper.updateManuscriptSpecial(manuscriptId, "Processing", newStatus);
        saveLog("Rescind", manuscriptId, "撤销决定，新状态: " + newStatus + "，原因: " + reason);
    }

    /**
     * 保存操作日志
     * 记录所有重要操作的审计日志
     * @param type 操作类型
     * @param manuscriptId 稿件ID（可为null）
     * @param desc 操作描述
     */
    private void saveLog(String type, Integer manuscriptId, String desc) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // === 修复点：增加空值判断 ===
        Integer operatorId = 1; // 默认给一个系统管理员ID，防止报错
        try {
            if (UserContext.getUserId() != null) {
                operatorId = UserContext.getUserId();
            }
        } catch (Exception e) {
            // 忽略上下文获取失败，使用默认ID
            System.err.println("警告：无法获取当前登录用户ID，使用默认ID记录日志");
        }

        // 确保 desc 不为空，防止数据库报错
        if (desc == null) desc = "无备注";

        userMapper.insertLog(now, operatorId, type, manuscriptId, desc);
    }
}