package com.bjfu.cms.service.impl;

import com.bjfu.cms.common.utils.UserContext;
import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.mapper.EditorMapper;
import com.bjfu.cms.mapper.ManuscriptMapper;
import com.bjfu.cms.service.EditorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class EditorServiceImpl implements EditorService {

    @Autowired
    private EditorMapper editorMapper;

    @Autowired
    private ManuscriptMapper manuscriptMapper;

    @Override
    public List<Manuscript> getAssignedList() {
        Integer loginId = UserContext.getUserId();
        // 【关键调试点】在控制台查看打印出的 ID 到底是多少

        return editorMapper.selectMyManuscripts(loginId);
    }

    @Override
    @Transactional // 涉及多表更新，开启事务
    public void inviteReviewers(Integer msId, List<Integer> rIds) {
        // 在方法内部生成日期，不要从接口传进来
        Calendar cal = Calendar.getInstance();
        Date inviteDate = cal.getTime(); // 当前时间

        cal.add(Calendar.DAY_OF_YEAR, 21);
        Date deadline = cal.getTime(); // 21天后

        // 【核对重点】这里的参数个数必须和 EditorMapper.java 接口里的定义一模一样！
        // 报错提示你“需要4个，找到3个”，说明你这里少写了一个变量
        editorMapper.batchInsertReviews(msId, rIds, inviteDate, deadline);

        // 更新状态
        editorMapper.updateManuscriptStatus(msId, "Processing", "UnderReview");
    }

    @Override
    public void submitToEIC(Manuscript m) {
        m.setCurrentEditorId(UserContext.getUserId());
        editorMapper.updateRecommendation(m);
    }

    @Override
    public Manuscript getById(Integer id) {
        // 直接返回根据 ID 查询的结果
        return manuscriptMapper.selectById(id);
    }
}