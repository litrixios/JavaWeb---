package com.bjfu.cms.service.impl;

import com.bjfu.cms.common.utils.UserContext;
import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.mapper.EditorMapper;
import com.bjfu.cms.service.EditorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Calendar;
import java.util.List;

@Service
public class EditorServiceImpl implements EditorService {

    @Autowired
    private EditorMapper editorMapper;

    @Override
    public List<Manuscript> getAssignedList() {
        return editorMapper.selectMyManuscripts(UserContext.getUserId());
    }

    @Override
    @Transactional // 涉及多表更新，开启事务
    public void inviteReviewers(Integer msId, List<Integer> rIds) {
        // 设置21天后的审稿截止日期
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 21);

        // 1. 插入审稿任务
        editorMapper.batchInsertReviews(msId, rIds, cal.getTime());
        // 2. 更新稿件小状态为 UnderReview
        editorMapper.updateSubStatus(msId, "UnderReview");
    }

    @Override
    public void submitToEIC(Manuscript m) {
        m.setCurrentEditorId(UserContext.getUserId());
        editorMapper.updateRecommendation(m);
    }
}