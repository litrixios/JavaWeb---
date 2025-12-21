package com.bjfu.cms.mapper;

import com.bjfu.cms.entity.News;
import com.bjfu.cms.entity.File;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface NewsMapper {
    // 新闻管理相关
    List<News> selectAllNews(@Param("keyword") String keyword, @Param("startDate") String startDate, @Param("endDate") String endDate);
    News selectNewsById(@Param("newsId") Integer newsId);
    void insertNews(News news);
    void updateNews(News news);
    void deleteNews(@Param("newsId") Integer newsId);

    // 新增：插入新闻附件时，传入newsId
    void insertNewsFile(@Param("file") File file, @Param("newsId") Integer newsId);

    // 修正：根据新闻ID查询附件（关联NewsID字段）
    List<File> selectFilesByNewsId(@Param("newsId") Integer newsId);
    void deleteNewsFile(@Param("fileId") Integer fileId);
    // 根据id查询文件存储路径
    File selectFileById(@Param("fileId") Integer fileId);
}