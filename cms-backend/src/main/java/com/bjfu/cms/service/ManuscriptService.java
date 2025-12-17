package com.bjfu.cms.service;

import com.bjfu.cms.entity.Manuscript;
import java.util.List;

public interface ManuscriptService {
    // 投稿
    void submitManuscript(Manuscript manuscript);

    // 查看我的稿件
    List<Manuscript> getMyManuscripts();
}