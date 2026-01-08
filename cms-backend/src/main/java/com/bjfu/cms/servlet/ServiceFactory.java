// ServiceFactory.java
package com.bjfu.cms.servlet;

import com.bjfu.cms.service.EditorialBoardService;
import com.bjfu.cms.service.NewsService;
import com.bjfu.cms.service.impl.EditorialBoardServiceImpl;
import com.bjfu.cms.service.impl.NewsServiceImpl;

public class ServiceFactory {
    private static EditorialBoardService editorialBoardService;
    private static NewsService newsService;

    static {
        // 初始化服务实例（根据你的实际实现类调整）
        editorialBoardService = new EditorialBoardServiceImpl();
        newsService = new NewsServiceImpl();
    }

    public static EditorialBoardService getEditorialBoardService() {
        return editorialBoardService;
    }

    public static NewsService getNewsService() {
        return newsService;
    }
}