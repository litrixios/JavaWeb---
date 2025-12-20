package com.bjfu.cms.service.impl;

import com.bjfu.cms.common.utils.UserContext;
import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.dto.ManuscriptDTO;
import com.bjfu.cms.mapper.ManuscriptMapper;
import com.bjfu.cms.service.ManuscriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 务必加上事务

import java.util.Date;
import java.util.List;

@Service
public class ManuscriptServiceImpl implements ManuscriptService {

    @Autowired
    private ManuscriptMapper manuscriptMapper;

    @Override
    @Transactional(rollbackFor = Exception.class) // 保证稿件表和版本表同时成功或失败
    public void submitManuscript(ManuscriptDTO dto) {
        Integer currentUserId = UserContext.getUserId();
        Manuscript manuscript = new Manuscript();

        // 复制基本属性
        manuscript.setTitle(dto.getTitle());
        manuscript.setAbstractText(dto.getAbstractText());
        manuscript.setKeywords(dto.getKeywords());
        manuscript.setAuthorList(dto.getAuthorList());
        manuscript.setFundingInfo(dto.getFundingInfo());
        manuscript.setSubmissionTime(new Date());
        manuscript.setAuthorId(currentUserId);

        // 判断状态：保存草稿 OR 正式提交
        if ("SUBMIT".equalsIgnoreCase(dto.getActionType())) {
            manuscript.setStatus("Submitted"); // 任务书要求的状态 [cite: 523]
        } else {
            manuscript.setStatus("Incomplete"); // 任务书要求的状态 [cite: 325]
        }

        // 1. 处理稿件主体 (Insert 或 Update)
        if (dto.getManuscriptId() == null) {
            // 新投稿
            manuscriptMapper.insertManuscript(manuscript);
            // 此时 manuscript.getManuscriptId() 已经被 MyBatis 自动回填了
        } else {
            // 更新已有草稿
            manuscript.setManuscriptId(dto.getManuscriptId());
            // 简单校验一下是否是自己的稿件
            Manuscript exists = manuscriptMapper.selectById(dto.getManuscriptId());
            if (exists == null || !exists.getAuthorId().equals(currentUserId)) {
                throw new RuntimeException("无权操作此稿件");
            }
            manuscriptMapper.updateManuscript(manuscript);
        }

        // 2. 如果是正式提交，必须在 Versions 表记录文件版本 (作为 Version 1)
        if ("SUBMIT".equalsIgnoreCase(dto.getActionType())) {
            // 根据任务书，投稿时上传文件 [cite: 343]
            // 注意：这里假设是第1版。如果是修回逻辑，VersionNumber 需要查询最大值+1
            manuscriptMapper.insertVersion(
                    manuscript.getManuscriptId(),
                    1, // Version 1
                    dto.getOriginalFilePath(),
                    dto.getAnonymousFilePath(),
                    dto.getCoverLetterPath()
            );
        }
    }

    @Override
    public List<Manuscript> getMyManuscripts() {
        Integer currentUserId = UserContext.getUserId();
        return manuscriptMapper.selectByAuthorId(currentUserId);
    }
}