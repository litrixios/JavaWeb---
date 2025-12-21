package com.bjfu.cms.service.impl;

import com.bjfu.cms.common.utils.UserContext;
import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.ManuscriptMeta;
import com.bjfu.cms.entity.dto.ManuscriptDTO;
import com.bjfu.cms.mapper.ManuscriptMapper;
import com.bjfu.cms.mapper.ManuscriptMetaMapper;
import com.bjfu.cms.service.ManuscriptService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class ManuscriptServiceImpl implements ManuscriptService {

    @Autowired
    private ManuscriptMapper manuscriptMapper;

    @Autowired
    private ManuscriptMetaMapper manuscriptMetaMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class) // 保证稿件表和版本表同时成功或失败
    public void submitManuscript(ManuscriptDTO dto) {
        Integer userId = UserContext.getUserId();

        // 1. 组装主表数据 (Manuscript)
        Manuscript manuscript = new Manuscript();
        manuscript.setManuscriptId(dto.getManuscriptId()); // 如果是修改，ID不为空
        manuscript.setAuthorId(userId);
        manuscript.setTitle(dto.getTitle());
        manuscript.setAbstractText(dto.getAbstractText());
        manuscript.setKeywords(dto.getKeywords());
        manuscript.setFundingInfo(dto.getFundingInfo());
        manuscript.setSubmissionTime(new Date());

        // 设置状态
        if ("SUBMIT".equalsIgnoreCase(dto.getActionType())) {
            // 正式提交 -> 进入 tech check
            manuscript.setStatus("Processing");
            manuscript.setSubStatus("TechCheck");
        } else {
            // 保存草稿
            manuscript.setStatus("Incomplete");
            manuscript.setSubStatus(null);
        }

        // 【关键】将作者列表转为 JSON 字符串存入原表 AuthorList 字段
        try {
            if (dto.getAuthors() != null) {
                manuscript.setAuthorList(objectMapper.writeValueAsString(dto.getAuthors()));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("作者信息格式化失败", e);
        }

        // 2. 保存/更新主表
        if (manuscript.getManuscriptId() == null) {
            manuscriptMapper.insertManuscript(manuscript);
            // Mybatis 会自动回填 ID
            dto.setManuscriptId(manuscript.getManuscriptId());
        } else {
            manuscriptMapper.updateManuscript(manuscript);
        }

        // 3. 组装扩展表数据 (ManuscriptMeta)
        ManuscriptMeta meta = new ManuscriptMeta();
        meta.setManuscriptId(dto.getManuscriptId()); // 拿到主表的ID
        meta.setTopic(dto.getTopic());
        meta.setCoverLetterContent(dto.getCoverLetterContent());

        // 【关键】将推荐审稿人转为 JSON 字符串
        try {
            if (dto.getRecommendedReviewers() != null) {
                meta.setRecommendedReviewers(objectMapper.writeValueAsString(dto.getRecommendedReviewers()));
            }
        } catch (JsonProcessingException e) {
            meta.setRecommendedReviewers("[]");
        }

        // 4. 保存扩展表 (判断是新增还是更新)
        if (manuscriptMetaMapper.selectByManuscriptId(dto.getManuscriptId()) == null) {
            manuscriptMetaMapper.insert(meta);
        } else {
            manuscriptMetaMapper.updateByManuscriptId(meta);
        }

        // 5. 如果是正式提交，必须在 Versions 表记录文件版本 (作为 Version 1)
        // ... 在 submitManuscript 方法底部 ...
        if (dto.getOriginalFilePath() != null && !dto.getOriginalFilePath().isEmpty()) {
            int versionNumber = 1;

            manuscriptMapper.insertVersion(
                    manuscript.getManuscriptId(),
                    versionNumber,
                    dto.getOriginalFilePath(),
                    dto.getAnonymousFilePath(),
                    dto.getCoverLetterPath(),
                    null, // 首次投稿没有 MarkedFilePath
                    null  // 首次投稿没有 ResponseLetterPath
            );
        }

        // TODO: 发送邮件到作者邮箱
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitRevision(ManuscriptDTO dto) {
        // 1. 校验：确保稿件存在且处于“需要修回”状态
        Manuscript manuscript = manuscriptMapper.selectById(dto.getManuscriptId());
        if (manuscript == null) {
            throw new RuntimeException("稿件不存在");
        }
        // 只有 Decision 是 'Revise' 或者 Status 是 'Revision' 的稿件才能修回
        // 这里根据你的业务逻辑调整，通常是 Status='Revision'
        if (!"Revision".equalsIgnoreCase(manuscript.getStatus())) {
            // 也可以放宽限制，防止因为状态流转问题卡住
            // throw new RuntimeException("当前稿件状态不允许修回");
        }

        // 2. 计算新版本号
        Integer maxVersion = manuscriptMapper.selectMaxVersion(dto.getManuscriptId());
        int nextVersion = (maxVersion == null ? 0 : maxVersion) + 1;

        // 3. 插入新版本记录 (Versions表)
        manuscriptMapper.insertVersion(
                dto.getManuscriptId(),
                nextVersion,
                dto.getOriginalFilePath(),   // Clean Version
                dto.getAnonymousFilePath(),  // 依然可能有匿名版
                dto.getCoverLetterPath(),    // 可能有新的 Cover Letter
                dto.getMarkedFilePath(),     // 【重点】标记修改版
                dto.getResponseLetterPath()  // 【重点】回复信
        );

        // 4. 更新稿件主表状态
        // 修回后，稿件通常回到 "Processing" 状态，等待编辑检查或再次送审
        manuscript.setStatus("Processing");

        // 子状态：如果是大修，可能需要 "TechCheck"；如果是小修，可能直接 "WithEditor"
        // 简单起见，统一设为 "WithEditor" 让编辑处理，或者 "TechCheck"
        manuscript.setSubStatus("TechCheck");

        manuscript.setSubmissionTime(new Date()); // 更新最近提交时间

        // 清空旧的决策，因为进入新一轮了
        manuscript.setDecision(null);
        manuscript.setDecisionTime(null);

        manuscriptMapper.updateManuscript(manuscript);

        // TODO: 发送邮件通知编辑 "收到修回稿件"
    }

    @Override
    public PageInfo<Manuscript> getManuscriptList(Integer pageNum, Integer pageSize, String status, String subStatus) {
        Integer userId = UserContext.getUserId();

        // 处理空字符串
        if (status != null && status.trim().isEmpty()) status = null;
        if (subStatus != null && subStatus.trim().isEmpty()) subStatus = null;

        // 【核心修改】开启分页拦截
        // pageNum: 当前页码 (从1开始)
        // pageSize: 每页条数
        PageHelper.startPage(pageNum, pageSize);

        // 执行原有查询 (此时 SQL 会被自动改写)
        List<Manuscript> list = manuscriptMapper.selectManuscripts(userId, status, subStatus);

        // 用 PageInfo 包装查询结果，它包含了 total(总条数), pages(总页数) 等信息
        return new PageInfo<>(list);
    }
}