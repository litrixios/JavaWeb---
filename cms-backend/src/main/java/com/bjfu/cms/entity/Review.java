package com.bjfu.cms.entity;

import lombok.Data;
import java.util.Date;

/**
 * 审稿任务实体类（映射数据库中的Review表）
 * 对应数据库表：Review（审稿任务表）
 */
@Data // Lombok注解，自动生成getter、setter、toString等方法，简化代码
public class Review {
    // 数据库表字段对应（驼峰命名：数据库ReviewID → 代码reviewId）
    private Integer reviewId;          // 评审ID（对应ReviewID）
    private Integer manuscriptID;      // 稿件ID（对应ManuscriptID）
    private Integer reviewerID;        // 审稿人ID（对应ReviewerID）
    private String commentsToAuthor;   // 给作者的公开意见（对应CommentsToAuthor）
    private String confidentialComments; // 给编辑的保密意见（对应ConfidentialComments，存储拒绝理由）
    private Integer score;             // 打分（对应Score）
    private String suggestion;         // 建议（对应Suggestion）
    private Date inviteDate;           // 邀请日期（对应InviteDate）
    private Date deadline;             // 截止日期（对应Deadline）
    private Date submitTime;           // 意见提交时间（对应SubmitTime）
    private String status;             // 状态（对应Status：Invited/Accepted/Rejected/Completed/Overdue）

    // 【关联字段】从Manuscript表关联的字段（非数据库Review表原生字段，用于前端展示）
    private String manuscriptTitle;    // 稿件标题（对应Manuscript表的Title）
    private String manuscriptAbstract; // 稿件摘要（对应Manuscript表的Abstract）
}