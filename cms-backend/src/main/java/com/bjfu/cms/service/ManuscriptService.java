package com.bjfu.cms.service;

import com.bjfu.cms.entity.Manuscript;
import com.bjfu.cms.entity.dto.ManuscriptDTO; // 记得引入你刚才建的DTO
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.PageHelper;

import java.util.List;

public interface ManuscriptService {
    /**
     * 提交稿件或保存草稿
     * @param manuscriptDTO 前端传来的包含文件路径和操作类型的DTO
     */
    void submitManuscript(ManuscriptDTO manuscriptDTO);

    /**
     * 获取当前登录用户的稿件列表
     * @return 稿件列表
     */

    PageInfo<Manuscript> getManuscriptList(Integer pageNum, Integer pageSize, String status, String subStatus);

    // 提交修回
    void submitRevision(ManuscriptDTO dto);
}