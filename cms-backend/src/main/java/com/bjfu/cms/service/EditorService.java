package com.bjfu.cms.service;

import com.bjfu.cms.entity.Manuscript;
import java.util.List;

public interface EditorService {
    List<Manuscript> getAssignedList();
    void inviteReviewers(Integer msId, List<Integer> rIds);
    void submitToEIC(Manuscript m);
    Manuscript getById(Integer id);
}