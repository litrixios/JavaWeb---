package com.bjfu.cms.service;

import com.bjfu.cms.entity.dto.EicDecisionDTO;
import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.User;
import java.util.List;
import java.util.Map;

/**
 * 主编服务接口
 * 定义主编在稿件管理系统中的所有核心业务操作
 */
public interface EditorInChiefService {

    // ================== 全览权限 ==================

    /**
     * 全览所有稿件
     * @param statusFilter 状态筛选条件（可选），用于按状态过滤稿件
     * @return 稿件列表
     */
    List<Manuscript> getAllManuscripts(String statusFilter);

    /**
     * 根据状态分组统计
     * 获取稿件状态统计信息，包括各状态稿件数量和平均审稿天数
     * @return 统计信息Map
     */
    Map<String, Integer> getManuscriptStatistics();

    /**
     * 导出报表
     * 导出指定时间范围的稿件处理报表为Excel文件
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return Excel文件字节数组
     */
    byte[] exportReport(String startDate, String endDate);

    // ================== 初审功能 ==================

    /**
     * 初审 (Desk Review)
     * 对新投稿进行初步审查，决定直接拒稿或送审
     * @param dto 初审决策数据传输对象
     */
    void deskReview(EicDecisionDTO dto);

    /**
     * 批量初审
     * 批量进行初审操作，提高处理效率
     * @param dtos 初审决策数据传输对象列表
     */
    void batchDeskReview(List<EicDecisionDTO> dtos);

    // ================== 指派编辑功能 ==================

    /**
     * 指派编辑 (Assign Editor)
     * 将通过初审的稿件分配给特定编辑处理
     * @param dto 指派编辑数据传输对象
     */
    void assignEditor(EicDecisionDTO dto);

    /**
     * 获取编辑列表
     * 获取所有可指派的编辑列表
     * @return 编辑列表
     */
    List<User> getEditorList();

    /**
     * 根据专长筛选编辑
     * 根据研究方向筛选合适的编辑
     * @param expertise 研究方向/专长
     * @return 符合条件的编辑列表
     */
    List<User> getEditorsByExpertise(String expertise);

    // ================== 终审决策 ==================

    /**
     * 终审决策 (Final Decision)
     * 基于编辑建议和审稿意见，做出最终录用、拒稿或修改决定
     * @param dto 终审决策数据传输对象
     */
    void makeFinalDecision(EicDecisionDTO dto);

    // ================== 管理审稿人库 ==================

    /**
     * 邀请审稿人
     * 向系统添加新的审稿人账号
     * @param dto 审稿人邀请数据传输对象
     */
    void inviteReviewer(EicDecisionDTO dto);

    /**
     * 审核审稿人
     * 审批或拒绝审稿人加入申请
     * @param userId 用户ID
     * @param status 审核状态（1:通过，0:拒绝）
     */
    void auditReviewer(Integer userId, Integer status);

    /**
     * 移除审稿人
     * 将审稿人移出库（加入黑名单）
     * @param userId 用户ID
     * @param reason 移除原因
     */
    void removeReviewer(Integer userId, String reason);

    /**
     * 获取审稿人列表
     * 获取所有审稿人信息，包括待审核的审稿人
     * @return 审稿人列表
     */
    List<User> getReviewerList();

    // ================== 特殊权限 ==================

    /**
     * 撤稿
     * 撤回已发布的稿件
     * @param dto 撤稿数据传输对象
     */
    void retractManuscript(EicDecisionDTO dto);

    /**
     * 撤销决定
     * 撤销之前的稿件处理决定，将状态改回上一阶段
     * @param manuscriptId 稿件ID
     * @param newStatus 新的状态
     * @param reason 撤销原因
     */
    void rescindDecision(Integer manuscriptId, String newStatus, String reason);
}