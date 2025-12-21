package com.bjfu.cms.controller;

//import com.bjfu.cms.entity.CallForPapers;
import com.bjfu.cms.entity.EditorialBoard;
//import com.bjfu.cms.service.JournalService;
import com.bjfu.cms.service.EditorialBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller // 注意：这里必须用 @Controller，不能用 @RestController
public class AboutController {

    @Autowired
    private EditorialBoardService editorialBoardService;  // 变量名改为小写

    @GetMapping("/about")
    public String AboutPage(Model model) {
//        List<EditorialBoard> editorialBoard = journalService.getEditorialBoard();
//        model.addAttribute("editorialBoard", editorialBoard);
        List<Map<String, Object>> editorialBoard = editorialBoardService.getPublicList();
        System.out.println("获取到的编委数据数量: " + editorialBoard.size());
        for(Map<String, Object> map : editorialBoard){
            System.out.println("编委信息: " + map);
        }
        model.addAttribute("editorialBoard", editorialBoard);
        return "about";
    }
}