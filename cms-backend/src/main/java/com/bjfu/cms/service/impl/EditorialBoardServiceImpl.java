package com.bjfu.cms.service.impl;

import com.bjfu.cms.entity.EditorialBoard;
import com.bjfu.cms.mapper.EditorialBoardMapper;
import com.bjfu.cms.service.EditorialBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

@Service
public class EditorialBoardServiceImpl implements EditorialBoardService {

    @Autowired
    private EditorialBoardMapper boardMapper;

    @Override
    public List<Map<String, Object>> getPublicList() {
        return boardMapper.selectVisibleForPublic();
    }

    @Override
    public List<Map<String, Object>> getAdminList() {
        return boardMapper.selectAllForAdmin();
    }

    @Override
    @Transactional
    public void addMember(EditorialBoard board) {
        if (board.getIsVisible() == null) board.setIsVisible(true);
        boardMapper.insert(board);
    }

    @Override
    @Transactional
    public void updateMember(EditorialBoard board) {
        boardMapper.update(board);
    }

    @Override
    @Transactional
    public void toggleVisibility(Integer boardId, Boolean isVisible) {
        boardMapper.updateVisibility(boardId, isVisible);
    }

    @Override
    @Transactional
    public void removeMember(Integer boardId) {
        boardMapper.delete(boardId);
    }
}