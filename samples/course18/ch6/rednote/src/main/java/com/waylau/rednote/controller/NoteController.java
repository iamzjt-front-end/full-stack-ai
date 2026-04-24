package com.waylau.rednote.controller;

import com.waylau.rednote.common.StringUtil;
import com.waylau.rednote.dto.DeleteResponseDto;
import com.waylau.rednote.dto.NoteDetailDto;
import com.waylau.rednote.dto.NoteEditDto;
import com.waylau.rednote.dto.NotePublishDto;
import com.waylau.rednote.entity.Note;
import com.waylau.rednote.entity.User;
import com.waylau.rednote.exception.NoteNotFoundException;
import com.waylau.rednote.service.NoteService;
import com.waylau.rednote.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * NoteController 笔记控制器
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/18
 **/
@Controller
@RequestMapping("/note")
public class NoteController {
    private static final Logger log = LoggerFactory.getLogger(NoteController.class);

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
    /*public String publishNote(@Valid @ModelAttribute("note") NotePublishDto notePublishDto,
                              BindingResult bindingResult,
                              Model model) {
        // 验证表单
        if (bindingResult.hasErrors()) {
            model.addAttribute("note", notePublishDto);
            return "note-publish";
        } else {
            // 获取当前用户信息
            User user = userService.getCurrentUser();

            // 通过笔记服务创建笔记
            *//*noteService.createNote(notePublishDto, user);*//*
            Note note = noteService.createNote(notePublishDto, user);
            model.addAttribute("note", note);

            // 显示笔记发布成功页面
            return "note-publish-success";
        }
    }*/
    public ResponseEntity<?> publishNote(@Valid @ModelAttribute("note") NotePublishDto notePublishDto,
                              BindingResult bindingResult) {
        // 验证表单
        if (bindingResult.hasErrors()) {
            // 自定义错误响应
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );

            return ResponseEntity.badRequest().body(errors);
        } else {
            // 获取当前用户信息
            User user = userService.getCurrentUser();

            // 通过笔记服务创建笔记
            noteService.createNote(notePublishDto, user);

            // 返回成功响应
            return ResponseEntity.ok("笔记创建成功");
        }
    }

    /**
     * 显示笔记详情页面
     */
    @GetMapping("/{noteId}")
    /*public String showNoteDetail(@PathVariable Long noteId, Model model) {
        // 查询指定noteId的笔记
        Optional<Note> optionalNote = noteService.findNoteById(noteId);

        // 判定笔记是否存在，不存在则抛出异常
        if (!optionalNote.isPresent()) {
            throw new NoteNotFoundException("");
        }

        Note note = optionalNote.get();
        model.addAttribute("note", note);

        return "note-detail";
    }*/
    public ResponseEntity<?> showNoteDetail(@PathVariable Long noteId) {
        // 查询指定noteId的笔记
        Optional<Note> optionalNote = noteService.findNoteById(noteId);

        // 判定笔记是否存在，不存在则抛出异常
        if (!optionalNote.isPresent()) {
            throw new NoteNotFoundException("");
        }

        Note note = optionalNote.get();
        User currentUser = userService.getCurrentUser();
        // 将Note对象转为NoteDetailDto对象
        return ResponseEntity.ok(NoteDetailDto.toNoteDetailDto(note, currentUser));
    }

    /**
     * 显示笔记编辑页面
     */
    @GetMapping("/{noteId}/edit")
    @PreAuthorize("@noteServiceImpl.isAuthor(#noteId, authentication.name)")
    /*public String showEditForm(@PathVariable Long noteId, Model model) {
        // 查询指定noteId的笔记
        Optional<Note> optionalNote = noteService.findNoteById(noteId);

        // 判定笔记是否存在，不存在则抛出异常
        if (!optionalNote.isPresent()) {
            throw new NoteNotFoundException("");
        }

        *//*
        // 获取当前用户信息
        User user = userService.getCurrentUser();

        Note note = optionalNote.get();

        // 判定笔记是否属于当前用户，不属于则抛出异常
        if (!note.getAuthor().getUserId().equals(user.getUserId())) {
            throw new NoteNotFoundException("");
        }
        *//*
        Note note = optionalNote.get();

        // 将Note对象转为NoteEditDto对象
        NoteEditDto noteEditDto = new NoteEditDto();
        noteEditDto.setNoteId(note.getNoteId());
        noteEditDto.setTitle(note.getTitle());
        noteEditDto.setContent(note.getContent());
        noteEditDto.setCategory(note.getCategory());
        noteEditDto.setImages(note.getImages());

        // 话题的List要转为String
        noteEditDto.setTopics(StringUtil.joinToString(note.getTopics(), " "));

        model.addAttribute("note", noteEditDto);

        return "note-edit";
    }*/
    public ResponseEntity<?> showEditForm(@PathVariable Long noteId) {
        // 查询指定noteId的笔记
        Optional<Note> optionalNote = noteService.findNoteById(noteId);

        // 判定笔记是否存在，不存在则抛出异常
        if (!optionalNote.isPresent()) {
            throw new NoteNotFoundException("");
        }

        Note note = optionalNote.get();

        // 将Note对象转为NoteEditDto对象
        NoteEditDto noteEditDto = new NoteEditDto();
        noteEditDto.setNoteId(note.getNoteId());
        noteEditDto.setTitle(note.getTitle());
        noteEditDto.setContent(note.getContent());
        noteEditDto.setCategory(note.getCategory());
        noteEditDto.setImages(note.getImages());

        // 话题的List要转为String
        noteEditDto.setTopics(StringUtil.joinToString(note.getTopics(), " "));

        return ResponseEntity.ok(noteEditDto);
    }

    /**
     * 处理笔记编辑请求
     */
    @PostMapping("/{noteId}")
    @PreAuthorize("@noteServiceImpl.isAuthor(#noteId, authentication.name)")
    /*public String updateNote(@PathVariable Long noteId,
                             @Valid @ModelAttribute("note") NoteEditDto noteEditDto,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        // 验证表单
        if (result.hasErrors()) {
            model.addAttribute("note", noteEditDto);
            return "note-edit";
        }

        // 检查笔记是否存在
        Optional<Note> optionalNote = noteService.findNoteById(noteId);
        if (!optionalNote.isPresent()) {
            throw new NoteNotFoundException("");
        }

        Note note = optionalNote.get();

        try {
            noteService.updateNote(note, noteEditDto);
            redirectAttributes.addFlashAttribute("success", "笔记更新成功");
            return "redirect:/note/" + noteId;
        } catch (Exception e) {
            log.error("笔记更新失败：{}", e.getMessage(), e);

            model.addAttribute("error", "笔记更新失败：" + e.getMessage());
            model.addAttribute("note", noteEditDto);
            return "note-edit";
        }
    }*/
    public ResponseEntity<?> updateNote(@PathVariable Long noteId,
                             @Valid @RequestBody NoteEditDto noteEditDto,
                             BindingResult result) {
        // 验证表单
        if (result.hasErrors()) {
            // 自定义错误响应
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }

        // 检查笔记是否存在
        Optional<Note> optionalNote = noteService.findNoteById(noteId);
        if (!optionalNote.isPresent()) {
            throw new NoteNotFoundException("");
        }

        Note note = optionalNote.get();
        Map<String, String> map = new HashMap<>();
        try {
            noteService.updateNote(note, noteEditDto);
            map.put("success", "笔记更新成功");
            return ResponseEntity.ok(map);
        } catch (Exception e) {
            log.error("笔记更新失败：{}", e.getMessage(), e);
            map.put("error", "笔记更新失败：" + e.getMessage());
            return ResponseEntity.ok(map);
        }
    }

    /**
     * 处理删除笔记的请求
     */
    @DeleteMapping("/{noteId}")
    @PreAuthorize("@noteServiceImpl.isAuthor(#noteId, authentication.name)")
    public ResponseEntity<DeleteResponseDto> deleteNote(@PathVariable Long noteId) {
        // 检查笔记是否存在
        Optional<Note> optionalNote = noteService.findNoteById(noteId);
        if (!optionalNote.isPresent()) {
            throw new NoteNotFoundException("");
        }

        Note note = optionalNote.get();

        /*// 获取当前用户信息
        User user = userService.getCurrentUser();

        // 判定笔记是否属于当前用户，不属于则抛出异常
        if (!note.getAuthor().getUserId().equals(user.getUserId())) {
            throw new NoteNotFoundException("");
        }*/

        // 使用服务删除笔记
        noteService.deleteNote(note);

        // 返回响应的内容
        DeleteResponseDto deleteResponseDto = new DeleteResponseDto();
        deleteResponseDto.setMessage("笔记删除成功");
        deleteResponseDto.setRedirectUrl("/user/profile");

        return ResponseEntity.ok(deleteResponseDto);
    }

}
