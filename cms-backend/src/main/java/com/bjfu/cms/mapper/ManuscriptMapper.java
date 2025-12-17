package com.bjfu.cms.mapper;

import com.bjfu.cms.entity.Manuscript;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ManuscriptMapper {
    // 1. 插入新稿件 (投稿)
    int insertManuscript(Manuscript manuscript);

    // 2. 根据用户ID查找稿件 (作者看自己的稿件列表)
    List<Manuscript> selectByAuthorId(@Param("authorId") Integer authorId);

//    // 3. 根据ID查详情
//    Manuscript selectById(@Param("id") Integer id);
}