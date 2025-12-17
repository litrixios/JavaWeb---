package com.bjfu.cms.service.impl;

import com.bjfu.cms.common.utils.UserContext;
import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.mapper.ManuscriptMapper;
import com.bjfu.cms.service.ManuscriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ManuscriptServiceImpl implements ManuscriptService {

    @Autowired
    private ManuscriptMapper manuscriptMapper;

    @Override
    public void submitManuscript(Manuscript manuscript) {
        // 1. 从 UserContext 获取当前登录的用户ID (这一步就是你保留它的价值！)
        Integer currentUserId = UserContext.getUserId();
        manuscript.setAuthorId(currentUserId);

        // 2. 补全默认信息
        manuscript.setStatus("Submitted"); // 初始状态
        manuscript.setSubmissionTime(new Date());

        // 3. 存入数据库
        manuscriptMapper.insertManuscript(manuscript);

        // 注意：如果是正式开发，这里还得处理文件上传逻辑，这只是元数据
    }

    @Override
    public List<Manuscript> getMyManuscripts() {
        // 自动获取当前用户ID，查他自己的稿子
        Integer currentUserId = UserContext.getUserId();
        return manuscriptMapper.selectByAuthorId(currentUserId);
    }
}