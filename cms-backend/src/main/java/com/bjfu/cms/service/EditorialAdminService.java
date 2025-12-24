package com.bjfu.cms.service;

import com.bjfu.cms.common.result.Result;
import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.dto.TechCheckAnalysisDTO;
import com.bjfu.cms.entity.dto.TechCheckDTO;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public interface EditorialAdminService {
    // 获取待形式审查的稿件列表
    List<Manuscript> getTechCheckManuscripts();

    // 形式审查
    void techCheck(TechCheckDTO dto);

    // 退回修改
    void unsubmitManuscript(TechCheckDTO dto);

    /**
     * 获取稿件最新版本文件并进行自动分析
     */
    Result<TechCheckAnalysisDTO> getTechCheckFileAnalysis(Integer manuscriptId);

    void previewLatestPdf(Integer manuscriptId, HttpServletResponse response);
}