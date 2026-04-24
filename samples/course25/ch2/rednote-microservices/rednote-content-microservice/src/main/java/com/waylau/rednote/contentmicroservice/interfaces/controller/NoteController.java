package com.waylau.rednote.contentmicroservice.interfaces.controller;

import com.waylau.rednote.common.StringUtil;
import com.waylau.rednote.common.dto.DeleteResponseDto;
import com.waylau.rednote.common.dto.NoteExploreDto;
import com.waylau.rednote.common.dto.NotesWithUserDto;
import com.waylau.rednote.common.dto.UserDto;
import com.waylau.rednote.common.interfaces.client.UserServiceClient;
import com.waylau.rednote.contentmicroservice.application.dto.NoteDetailDto;
import com.waylau.rednote.contentmicroservice.application.dto.NoteEditDto;
import com.waylau.rednote.contentmicroservice.application.dto.NotePublishDto;
import com.waylau.rednote.contentmicroservice.domain.model.entity.Note;
import com.waylau.rednote.common.exception.NoteNotFoundException;
import com.waylau.rednote.contentmicroservice.application.service.NoteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.HashMap;
import java.util.List;
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
    //private UserService userService;
    private UserServiceClient userServiceClient;

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
    public ResponseEntity<?> publishNote(@Valid @ModelAttribute("note") NotePublishDto notePublishDto,
                                         BindingResult bindingResult,
                                         HttpServletRequest request) {
        // 添加调试信息
        log.info("Content-Type: {}", request.getContentType());

        if (request instanceof MultipartHttpServletRequest) {
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            log.info("Multipart parameter names: {}", multipartRequest.getParameterNames());
            if (multipartRequest.getFileMap().containsKey("images")) {
                log.info("Number of 'images' files: {}", multipartRequest.getFiles("images").size());
            }
        }

        log.info("Images count: {}", notePublishDto.getImages().size());
        for (int i = 0; i < notePublishDto.getImages().size(); i++) {
            MultipartFile file = notePublishDto.getImages().get(i);
            log.info("Image {}: name={}, size={}", i, file.getOriginalFilename(), file.getSize());
        }
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
            /*User user = userService.getCurrentUser();*/
            UserDto userDto = userServiceClient.getCurrentUser().getBody();

            // 通过笔记服务创建笔记
            noteService.createNote(notePublishDto, userDto);

            // 返回成功响应
            return ResponseEntity.ok("笔记创建成功");
        }
    }

    /**
     * 显示笔记详情页面
     */
    @GetMapping("/{noteId}")
    public ResponseEntity<?> showNoteDetail(@PathVariable Long noteId) {
        // 查询指定noteId的笔记
        Optional<Note> optionalNote = noteService.findNoteById(noteId);

        // 判定笔记是否存在，不存在则抛出异常
        if (!optionalNote.isPresent()) {
            throw new NoteNotFoundException("");
        }

        Note note = optionalNote.get();
        /*User currentUser = userService.getCurrentUser();*/
        UserDto author = userServiceClient.findByUserId(note.getUserId()).getBody();

        // 将Note对象转为NoteDetailDto对象
        return ResponseEntity.ok(NoteDetailDto.toNoteDetailDto(note, author));
    }

    /**
     * 显示笔记编辑页面
     */
    @GetMapping("/{noteId}/edit")
    @PreAuthorize("@noteServiceImpl.isAuthor(#noteId, authentication.name)")
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

        // 使用服务删除笔记
        noteService.deleteNote(note);

        // 返回响应的内容
        DeleteResponseDto deleteResponseDto = new DeleteResponseDto();
        deleteResponseDto.setMessage("笔记删除成功");
        deleteResponseDto.setRedirectUrl("/user/profile");

        return ResponseEntity.ok(deleteResponseDto);
    }

    @GetMapping("/user/{userId}")
    ResponseEntity<NotesWithUserDto> getNotesByUser(@PathVariable Long userId,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "12") int size) {
        // 获取用户信息
        ResponseEntity<UserDto> response = userServiceClient.findByUserId(userId);
        UserDto user = response.getBody();

        // 获取用户笔记列表数据
        Page<Note> notePage = noteService.getNotesByUser(userId, page - 1, size);

        // 转为DTO
        List<NoteExploreDto> noteExploreDtoList =
                notePage.map(note -> noteService.toExploreDto(note, user)).getContent();

        NotesWithUserDto notesWithUserDto
                = new NotesWithUserDto(user, noteExploreDtoList, page, notePage.getTotalPages());
        ResponseEntity<NotesWithUserDto> responseEntity = ResponseEntity.ok(notesWithUserDto);

        return responseEntity;
    }

}
