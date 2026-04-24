package com.waylau.rednote.controller;

import com.waylau.rednote.dto.NotePublishDto;
import com.waylau.rednote.entity.User;
import com.waylau.rednote.service.NoteService;
import com.waylau.rednote.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * NoteController 笔记控制器 
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/18
**/
@Controller
@RequestMapping("/note")
public class NoteController {

    @Autowired
    private UserService userService;

    @Autowired
    private NoteService noteService;

    /**
     * 显示笔记发布页面
     */
    @GetMapping("/publish")
    public String showPublishForm(Model model) {
        model.addAttribute("note", new NotePublishDto());
        return "note-publish";
    }

    /**
     * 处理笔记发布请求
     */
    @PostMapping("/publish")
    public String publishNote(@Valid @ModelAttribute("note") NotePublishDto notePublishDto,
                              BindingResult bindingResult,
                              Model model){
        // 验证表单
        if (bindingResult.hasErrors()) {
            model.addAttribute("note", notePublishDto);
            return "note-publish";
        } else {
            // 获取当前用户信息
            User user = userService.getCurrentUser();

            // 通过笔记服务创建笔记
            noteService.createNote(notePublishDto, user);

            // 显示笔记发布成功页面
            return "note-publish-success";
        }
    }
}
