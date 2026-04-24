package com.waylau.rednote.controller;

import com.waylau.rednote.dto.NoteExploreDto;
import com.waylau.rednote.dto.NoteResponseDto;
import com.waylau.rednote.entity.Note;
import com.waylau.rednote.entity.User;
import com.waylau.rednote.service.NoteService;
import com.waylau.rednote.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 * ExploreController 首页笔记探索
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/20
 **/
@Controller
@RequestMapping("/explore")
public class ExploreController {

    public static final String DEFAULT_CATEGORY = "推荐";
    public static final int PAGE_SIZE = 20;

    @Autowired
    private NoteService noteService;

    @Autowired
    private UserService userService;

    /**
     * 显示笔记探索页面
     */
    @GetMapping
    public String showExplore() {
        return "explore";
    }

    /**
     * 处理笔记探索页面的数据加载的请求
     */
    @GetMapping("/note")
    /*public ResponseEntity<NoteResponseDto> getNotesByCategory(@RequestParam(defaultValue = "1") int page,
                                                              @RequestParam(required = false) String category) {*/
    public ResponseEntity<NoteResponseDto> getNotesByCategory(@RequestParam(defaultValue = "1") int page,
                                                              @RequestParam(required = false) String category,
                                                              @RequestParam(required = false) String query) {
        // 注意：把分类“推荐”当成null
        if (DEFAULT_CATEGORY.equals(category)) {
            category = null;
        }

        // 分页查询笔记
        Page<Note> notes = null;
        // 判定query是否为空
        if (query != null && query.trim().length() > 0) {
            notes = noteService.getNotesByPageAndQuery(page, PAGE_SIZE, category, query);
        } else {
            notes = noteService.getNotesByPage(page, PAGE_SIZE, category);
        }

        NoteResponseDto noteResponseDto = new NoteResponseDto();
        /*noteResponseDto.setNotes(notes.getContent());*/
        noteResponseDto.setHasMore(notes.hasNext());

        User user = userService.getCurrentUser();

        // 处理序列化问题
        List<NoteExploreDto> noteExploreDtoLst = new ArrayList<>();
        for (Note note : notes.getContent()) {
            /*noteExploreDtoLst.add(NoteExploreDto.toExploreDto(note));*/
            noteExploreDtoLst.add(NoteExploreDto.toExploreDto(note, user));
        }
        noteResponseDto.setNotes(noteExploreDtoLst);

        return ResponseEntity.ok(noteResponseDto);
    }
}
