package com.bjfu.cms.service.impl;

import com.bjfu.cms.common.utils.UserContext;
import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.User;
import com.bjfu.cms.mapper.EditorMapper;
import com.bjfu.cms.mapper.ManuscriptMapper; // 确保有这个Mapper
import com.bjfu.cms.service.EditorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;

@Service
public class EditorServiceImpl implements EditorService {

    @Autowired
    private EditorMapper editorMapper;

    // 修复点 1: 必须注入 ManuscriptMapper 才能调用 selectById
    @Autowired
    private ManuscriptMapper manuscriptMapper;

    @Override
    public List<Manuscript> getAssignedList() {
        // 修复点 2: 补充缺失的接口实现，从当前上下文获取编辑ID
        Integer editorId = UserContext.getUserId();
        return editorMapper.selectMyManuscripts(editorId);
    }

    @Override
    public List<User> getAllReviewers() {
        return editorMapper.selectUsersByRole("reviewer");
    }

    @Override
    @Transactional
    public void inviteReviewers(Integer msId, List<Integer> rIds) {
        Date now = new Date();
        // 设置 14 天后截止
        Date deadline = new Date(System.currentTimeMillis() + 14L * 24 * 60 * 60 * 1000);

        // 1. 批量插入审稿记录
        editorMapper.batchInsertReviews(msId, rIds, now, deadline);

        // 2. 更新稿件状态
        // 这里的参数需对应你 Mapper.xml 中的定义
        editorMapper.updateManuscriptStatus(msId, "Under Review", "Reviewing");
    }

    @Override
    public void submitToEIC(Manuscript m) {
        // 确保 manuscriptId 已从前端传回
        m.setCurrentEditorId(UserContext.getUserId());
        editorMapper.updateRecommendation(m);
    }

    @Override
    public Manuscript getById(Integer id) {
        // 修复点 3: 确保 manuscriptMapper 已注入且有 selectById 方法
        // 如果没有通用 Mapper，则需要在 EditorMapper 里写 SQL
        return manuscriptMapper.selectById(id);
    }
} // 所有的实现方法必须在这个大括号内！