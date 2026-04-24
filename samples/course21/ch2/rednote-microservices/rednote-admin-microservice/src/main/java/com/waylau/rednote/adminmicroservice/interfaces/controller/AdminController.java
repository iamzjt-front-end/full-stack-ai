package com.waylau.rednote.adminmicroservice.interfaces.controller;

import com.waylau.rednote.common.dto.DeleteResponseDto;
import com.waylau.rednote.contentmicroservice.application.dto.NoteBrowseCountDto;
import com.waylau.rednote.contentmicroservice.application.dto.NoteBrowseTimeDto;
import com.waylau.rednote.common.entity.User;
import com.waylau.rednote.common.exception.UserNotFoundException;
import com.waylau.rednote.contentmicroservice.application.service.CommentService;
import com.waylau.rednote.contentmicroservice.application.service.NoteService;
import com.waylau.rednote.usermicroservice.application.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * AdminController 后台管理控制器
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/23
 **/
@Controller
@RequestMapping("/admin")
public class AdminController {
    private static final int PAGE_SIZE = 10;

    @Autowired
    private UserService userService;

    @Autowired
    private NoteService noteService;

    @Autowired
    private CommentService commentService;

    /**
     * 显示数据看板界面
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard() {
        // 获取统计数据
        long userCount = userService.countUsers();
        long noteCount = noteService.countNotes();
        long commentCount = commentService.countComments();
        List<NoteBrowseCountDto> noteBrowseCountDtoList = noteService.getNotesByBrowseCount(1, 10);
        List<NoteBrowseTimeDto> noteBrowseTimeDtoList = noteService.getNotesByBrowseTime(1, 10);

        Map<String, Object> map = new HashMap<>();
        map.put("userCount", userCount);
        map.put("noteCount", noteCount);
        map.put("commentCount", commentCount);
        map.put("noteBrowseCountDtoList", noteBrowseCountDtoList);
        map.put("noteBrowseTimeDtoList", noteBrowseTimeDtoList);

        return ResponseEntity.ok(map);
    }

    /**
     * 显示用户管理界面
     */
    @GetMapping("/user")
    public ResponseEntity<?> user(@RequestParam(defaultValue = "1") int page) {
        // 分页查询所有用户数据
        Page<User> userPage = userService.getAllUsers(page, PAGE_SIZE);

        Map<String, Object> map = new HashMap<>();
        map.put("userList", userPage.getContent());
        map.put("currentPage", page);
        map.put("totalPages", userPage.getTotalPages());

        return ResponseEntity.ok(map);
    }

    /**
     * 显示笔记管理界面
     */
    @GetMapping("/note")
    public String note(Model model) {
        model.addAttribute("contentFragment", "admin-note");
        return "admin";
    }

    /**
     * 显示评论管理界面
     */
    @GetMapping("/comment")
    public String comment(Model model) {
        model.addAttribute("contentFragment", "admin-comment");
        return "admin";
    }

    /**
     * 显示用户编辑界面
     */
    @GetMapping("/user/{userId}/edit")
    public ResponseEntity<?> editUser(@PathVariable Long userId) {
        // 判定用户是否存在，不存在则抛出异常
        Optional<User> optionalUser = userService.findByUserId(userId);
        if (!optionalUser.isPresent()) {
            throw new UserNotFoundException("");
        }

        return ResponseEntity.ok(optionalUser.get());
    }

    /**
     * 处理保存用户的请求
     */
    @PostMapping("/user")
    public ResponseEntity<?> updateUser(@ModelAttribute User user) {
        // 判定用户是否存在，不存在则抛出异常
        Optional<User> optionalUser = userService.findByUserId(user.getUserId());
        if (!optionalUser.isPresent()) {
            throw new UserNotFoundException("");
        }

        User oldUser = optionalUser.get();

        // 更新用户
        userService.updateUserByAdmin(oldUser, user);
        return ResponseEntity.ok("更新成功");
    }

    /**
     * 处理用户删除的请求
     */
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<DeleteResponseDto> deleteUser(@PathVariable Long userId) {
        // 判定用户是否存在，不存在则抛出异常
        Optional<User> optionalUser = userService.findByUserId(userId);
        if (!optionalUser.isPresent()) {
            throw new UserNotFoundException("");
        }

        userService.deleteUser(userId);

        DeleteResponseDto deleteResponseDto = new DeleteResponseDto();
        deleteResponseDto.setMessage("用户删除成功");
        deleteResponseDto.setRedirectUrl("/admin/user");

        return ResponseEntity.ok(deleteResponseDto);
    }
}
