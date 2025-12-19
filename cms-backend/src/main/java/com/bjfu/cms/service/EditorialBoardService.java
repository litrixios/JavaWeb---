package com.bjfu.cms.service;

import com.bjfu.cms.entity.EditorialBoard;
import java.util.List;
import java.util.Map;

public interface EditorialBoardService {
    List<Map<String, Object>> getPublicList();
    List<Map<String, Object>> getAdminList();
    void addMember(EditorialBoard board);
    void updateMember(EditorialBoard board);
    void toggleVisibility(Integer boardId, Boolean isVisible);
    void removeMember(Integer boardId);
}