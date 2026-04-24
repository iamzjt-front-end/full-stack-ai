package com.waylau.rednote.controller;

import com.waylau.rednote.dto.DeleteResponseDto;
import com.waylau.rednote.entity.User;
import com.waylau.rednote.exception.UserNotFoundException;
import com.waylau.rednote.service.CommentService;
import com.waylau.rednote.service.NoteService;
import com.waylau.rednote.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping()
    public String goToAdmin() {
        /*return "admin";*/
        // 重定向到第一个功能界面“数据看板”
        return "redirect:/admin/dashboard";
    }

    /**
     * 显示数据看板界面
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // 获取统计数据
        long userCount = userService.countUsers();
        long noteCount = noteService.countNotes();
        long commentCount = commentService.countComments();

        model.addAttribute("userCount", userCount);
        model.addAttribute("noteCount", noteCount);
        model.addAttribute("commentCount", commentCount);
        model.addAttribute("contentFragment", "admin-dashboard");
        return "admin";
    }

    /**
     * 显示用户管理界面
     */
    @GetMapping("/user")
    /*public String user(Model model) {*/
    public String user(Model model, @RequestParam(defaultValue = "1") int page) {
        // 分页查询所有用户数据
        Page<User> userPage = userService.getAllUsers(page, PAGE_SIZE);

        model.addAttribute("userPage", userPage);
        model.addAttribute("contentFragment", "admin-user");
        return "admin";
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
    public String editUser(@PathVariable Long userId, Model model) {
        // 判定用户是否存在，不存在则抛出异常
        Optional<User> optionalUser = userService.findByUserId(userId);
        if (!optionalUser.isPresent()) {
            throw new UserNotFoundException("");
        }

        model.addAttribute("user", optionalUser.get());
        model.addAttribute("contentFragment", "admin-user-edit");

        return "admin";
    }

    /**
     * 处理保存用户的请求
     */
    @PostMapping("/user")
    public String updateUser(@ModelAttribute User user) {
        // 判定用户是否存在，不存在则抛出异常
        Optional<User> optionalUser = userService.findByUserId(user.getUserId());
        if (!optionalUser.isPresent()) {
            throw new UserNotFoundException("");
        }

        User oldUser = optionalUser.get();

        // 更新用户
        userService.updateUserByAdmin(oldUser, user);
        return "redirect:/admin/user";
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
