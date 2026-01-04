package com.bjfu.cms.service.impl;

import com.bjfu.cms.common.utils.UserContext;
import com.bjfu.cms.entity.SystemLog;
import com.bjfu.cms.entity.User;
import com.bjfu.cms.entity.dto.EicDecisionDTO;
import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.dto.ManuscriptDetailDTO;
import com.bjfu.cms.mapper.ManuscriptMapper;
import com.bjfu.cms.mapper.UserMapper;
import com.bjfu.cms.service.CommunicationService;
import com.bjfu.cms.service.EditorInChiefService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EditorInChiefServiceImpl implements EditorInChiefService {

    @Autowired
    private CommunicationService communicationService;

    @Autowired
    private ManuscriptMapper manuscriptMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<Manuscript> getAllManuscripts(String statusFilter) {
        return manuscriptMapper.selectAllManuscripts(statusFilter);
    }

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
            String editorRecommendation = manuscript.getEditorRecommendation();
            String editorSummaryReport = manuscript.getEditorSummaryReport();

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
            // 4. 正在审稿：必须满足四个条件：
            //    - status='Processing'
            //    - subStatus='UnderReview'
            //    - editorRecommendation 不为空且非空字符串
            //    - editorSummaryReport 不为空且非空字符串
            else if ("Processing".equals(status)
                    && "UnderReview".equals(subStatus)
                    && editorRecommendation != null
                    && !editorRecommendation.trim().isEmpty()
                    && editorSummaryReport != null
                    && !editorSummaryReport.trim().isEmpty()) {
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

                communicationService.sendMessage(
                        1,
                        manuscript.getAuthorId(),
                        "MS-" + dto.getManuscriptId(),
                        "初审通过通知",
                        "您的稿件已通过初审，正在安排编辑处理。", 0
                );
            }
        } catch (Exception e) {
            saveLog("DeskReviewError", dto.getManuscriptId(),
                    "初审失败: " + e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public void batchDeskReview(List<EicDecisionDTO> dtos) {
        for (EicDecisionDTO dto : dtos) {
            deskReview(dto);
        }
    }

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

        communicationService.sendMessage(
                1,
                dto.getEditorId(),
                "MS-" + dto.getManuscriptId(),
                "新稿件指派",
                "您有新的稿件待处理。稿件ID：" + dto.getManuscriptId(),
                0
        );
    }

    @Override
    public List<User> getEditorList() {
        // 查询所有编辑（角色为Editor或AssociateEditor）
        return userMapper.selectEditors();
    }

    @Override
    public List<User> getEditorsByExpertise(String expertise) {
        List<User> allEditors = getEditorList();

        // 根据专长筛选
        return allEditors.stream()
                .filter(editor -> editor.getResearchDirection() != null &&
                        editor.getResearchDirection().toLowerCase().contains(expertise.toLowerCase()))
                .collect(Collectors.toList());
    }

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

        Manuscript m = manuscriptMapper.selectById(dto.getManuscriptId());
        communicationService.sendMessage(
                1,
                m.getAuthorId(),
                "MS-" + dto.getManuscriptId(),
                "终审结果通知",
                "您的稿件有了新的决定：" + dto.getDecision() + "。备注：" + dto.getComments(),
                0
        );
    }

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

    @Override
    public List<User> getReviewerList() {
        return userMapper.selectByRole("Reviewer");
    }

    @Override
    @Transactional
    public void auditReviewer(Integer userId, Integer status) {
        userMapper.updateUserStatus(userId, status);
        String statusText = status == 1 ? "批准" : "拒绝";
        saveLog("AuditReviewer", null,
                "审核审稿人ID: " + userId + "，结果: " + statusText);
    }

    @Override
    @Transactional
    public void removeReviewer(Integer userId, String reason) {
        userMapper.updateUserStatus(userId, 2); // 状态设为2（移除/黑名单）
        saveLog("RemoveReviewer", null,
                "移除审稿人ID: " + userId + "，原因: " + reason);
    }

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

            communicationService.sendMessage(
                    1,
                    manuscript.getAuthorId(),
                    "MS-" + dto.getManuscriptId(),
                    "撤稿通知",
                    "您的稿件已被执行撤稿处理。原因：" + dto.getComments(),
                    0
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
    @Override
    public ManuscriptDetailDTO getManuscriptDetails(Integer manuscriptId) {
        ManuscriptDetailDTO dto = new ManuscriptDetailDTO();

        // 1. 获取稿件基本信息
        Manuscript manuscript = manuscriptMapper.selectById(manuscriptId);
        if (manuscript == null) {
            throw new RuntimeException("稿件不存在");
        }
        dto.setManuscript(manuscript);

        // 2. 获取历史日志 (已有的 mapper 方法)
        List<SystemLog> logs = manuscriptMapper.selectLogsByManuscriptId(manuscriptId);
        dto.setHistoryLogs(logs);

        // 3. 获取审稿意见摘要
        List<Map<String, Object>> reviews = manuscriptMapper.selectReviewsByManuscriptId(manuscriptId);
        dto.setReviewSummaries(reviews);

        return dto;
    }

    @Override
    public byte[] exportReport(String startDate, String endDate) {
        // 1. 查询数据
        // 注意：前端传来的可能是 "yyyy-MM-dd"，需确保格式匹配数据库或在此处转换
        List<Manuscript> list = manuscriptMapper.selectManuscriptsByDateRange(startDate + " 00:00:00", endDate + " 23:59:59");

        // 2. 创建 Excel
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("稿件统计报表");

            // 3. 创建表头
            Row headerRow = sheet.createRow(0);
            String[] columns = {"稿件ID", "标题", "作者", "提交时间", "当前状态", "最终决定", "处理时长(天)"};

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // 4. 填充数据
            int rowNum = 1;
            long totalDays = 0;
            int decidedCount = 0;

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (Manuscript m : list) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(m.getManuscriptId());
                row.createCell(1).setCellValue(m.getTitle());

                // 获取作者名 (假设 selectManuscriptsByDateRange 的 resultMap 没联表，可能需要单独处理或优化SQL，这里假设已联表或只显示ID)
                // 建议优化 SQL 使用 selectAllManuscripts 的逻辑联表查询作者名
                row.createCell(2).setCellValue(m.getAuthorId());

                row.createCell(3).setCellValue(m.getSubmissionTime() != null ? m.getSubmissionTime().toString() : "");
                row.createCell(4).setCellValue(m.getStatus() + " - " + m.getSubStatus());
                row.createCell(5).setCellValue(m.getDecision() != null ? m.getDecision() : "-");

                // 计算处理时长
                long days = 0;
                if (m.getDecisionTime() != null && m.getSubmissionTime() != null) {
                    long diff = m.getDecisionTime().getTime() - m.getSubmissionTime().getTime();
                    days = diff / (1000 * 60 * 60 * 24);
                    totalDays += days;
                    decidedCount++;
                }
                row.createCell(6).setCellValue(m.getDecisionTime() != null ? String.valueOf(days) : "-");
            }

            // 5. 添加统计行
            Row statRow = sheet.createRow(rowNum + 1);
            statRow.createCell(0).setCellValue("统计汇总：");
            statRow.createCell(1).setCellValue("总稿件数：" + list.size());

            double avgTime = decidedCount > 0 ? (double) totalDays / decidedCount : 0;
            statRow.createCell(3).setCellValue("平均审稿周期(天)：" + String.format("%.2f", avgTime));

            // 自动调整列宽
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(bos);
            return bos.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("生成Excel报表失败: " + e.getMessage());
        }
    }
    @Override
    @Transactional
    public void rescindDecision(Integer manuscriptId, String newStatus, String reason) {
        manuscriptMapper.updateManuscriptSpecial(manuscriptId, "Processing", newStatus);
        saveLog("Rescind", manuscriptId, "撤销决定，新状态: " + newStatus + "，原因: " + reason);

        Manuscript m = manuscriptMapper.selectById(manuscriptId);
        communicationService.sendMessage(
                1,
                m.getAuthorId(),
                "MS-" + manuscriptId, "决定撤销通知",
                "之前的审稿决定已被撤销，稿件状态已恢复为：" + newStatus + "。原因：" + reason,
                0
        );
    }

    private void saveLog(String type, Integer manuscriptId, String desc) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // === 修复点：增加空值判断 ===
        Integer operatorId = 2; // 默认给一个系统管理员ID，防止报错
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