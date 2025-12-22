package com.bjfu.cms.service;

import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.User;
import java.util.List;

public interface EditorService {
    List<Manuscript> getAssignedList();
    void inviteReviewers(Integer msId, List<Integer> rIds);
    void submitToEIC(Manuscript m);
    Manuscript getById(Integer id);
    // --- 新增 ---
    List<User> getAllReviewers();
    void sendRemindMail(Integer reviewId, String content);
}