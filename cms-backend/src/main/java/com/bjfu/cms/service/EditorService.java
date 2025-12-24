package com.bjfu.cms.service;

import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.User;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public interface EditorService {
    List<Manuscript> getAssignedList();
    void inviteReviewers(Integer msId, List<Integer> rIds);

    // 增加 manuscriptId 参数
    void sendRemindMail(Integer manuscriptId, Integer reviewId, String content);

    void submitToEIC(Manuscript m);
    Manuscript getById(Integer id);
    // --- 新增 ---
    List<User> getAllReviewers();

    // 增加这个方法声明
    void downloadManuscript(Integer id, HttpServletResponse response) throws Exception;
    /**
     * 根据稿件关键词和审稿人研究方向，智能推荐审稿人
     */
    List<User> recommendReviewers(Integer msId);
}