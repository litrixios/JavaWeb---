package com.bjfu.cms.service;

import com.bjfu.cms.entity.News;
import com.bjfu.cms.entity.File;
import com.bjfu.cms.common.result.Result;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import org.springframework.core.io.Resource;

public interface NewsService {
    // 新闻管理相关
    List<News> getAllNews(String keyword, String startDate, String endDate);
    News getNewsById(Integer newsId);
    Result<Integer> addNews(News news);
    Result<String> updateNews(News news);
    Result<String> deleteNews(Integer newsId);

    // 新闻附件相关
    Result<String> uploadNewsFile(Integer newsId, MultipartFile file);
    List<File> getFilesByNewsId(Integer newsId);
    Result<String> deleteNewsFile(Integer fileId);
    Result<Resource> downloadNewsFile(Integer fileId);
}