package com.waylau.rednote.controller;

import com.waylau.rednote.config.MongoConfig;
import com.waylau.rednote.dto.NoteExploreDto;
import com.waylau.rednote.entity.Note;
import com.waylau.rednote.entity.User;
import com.waylau.rednote.exception.UserNotFoundException;
/*import com.waylau.rednote.service.FileStorageService;*/
import com.waylau.rednote.service.GridFSStorageService;
import com.waylau.rednote.service.NoteService;
import com.waylau.rednote.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

/**
 * UserController 用户控制器
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/17
 **/
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /*@Autowired
    private FileStorageService fileStorageService;*/

    @Autowired
    private GridFSStorageService gridFSStorageService;

    @Autowired
    private NoteService noteService;

    @GetMapping("/profile")
    /*public String profile(Model model) {
        // 获取当前用户信息
        User user = userService.getCurrentUser();

        *//*model.addAttribute("user", user);

        return "user-profile";*//*

        // 重定向
        return "redirect:/user/profile/" + user.getUserId();
    }*/
    public ResponseEntity<User> profile() {
        // 获取当前用户信息
        User user = userService.getCurrentUser();

        return ResponseEntity.ok(user);
    }

    @GetMapping("/edit")
    /*public String editProfile(Model model) {
        User user = userService.getCurrentUser();

        model.addAttribute("user", user);

        return "user-profile-edit";
    }*/
    public ResponseEntity<User> editProfile() {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(user);
    }

    @Transactional
    @PostMapping("/edit")
    /*
    public String updateProfile(@ModelAttribute User user, RedirectAttributes redirectAttributes,
                                @RequestParam("avatarFile") MultipartFile avatarFile) {
        User currentUser = userService.getCurrentUser();
        String oldAvatar = currentUser.getAvatar();

        // 验证文件类型和大小
        if (avatarFile != null && !avatarFile.isEmpty()) {
            // 验证文件类型
            String contentType = avatarFile.getContentType();
            if (!contentType.startsWith("image/")) {
                redirectAttributes.addFlashAttribute("error", "请上传图片文件");

                return "redirect:/user/edit";
            }

            String fileId = gridFSStorageService.uploadImage(avatarFile);
            String fileUrl = MongoConfig.STATIC_PATH_PREFIX + fileId;

            currentUser.setAvatar(fileUrl);

            // 删除旧头像文件
            if (oldAvatar != null && !oldAvatar.isEmpty()) {
                String oldFileId = oldAvatar.substring(oldAvatar.lastIndexOf("/") + 1);
                gridFSStorageService.deleteImage(oldFileId);
            }
        }

        // 更新用户信息
        currentUser.setPhone(user.getPhone());
        currentUser.setBio(user.getBio());

        // 修改内容保存到数据库
        userService.updateUser(currentUser);

        // 重定向到指定页面，并传递参数
        redirectAttributes.addFlashAttribute("success", "个人信息更新成功");

        return "redirect:/user/profile";
    }
    */
    public ResponseEntity<?> updateProfile(@RequestParam(required = true) String phone,
                                           @RequestParam(required = false) String bio,
                                           @RequestParam(required = false, value = "avatarFile") MultipartFile avatarFile) {
        User currentUser = userService.getCurrentUser();
        String oldAvatar = currentUser.getAvatar();
        Map<String, String> map = new HashMap<>();

        // 验证文件类型和大小
        if (avatarFile != null && !avatarFile.isEmpty()) {
            // 验证文件类型
            String contentType = avatarFile.getContentType();
            if (!contentType.startsWith("image/")) {
                map.put("error", "请上传图片文件");
                return ResponseEntity.ok(map);
            }

            // 处理文件上传
            String fileId = gridFSStorageService.uploadImage(avatarFile);
            String fileUrl = MongoConfig.STATIC_PATH_PREFIX + fileId;

            currentUser.setAvatar(fileUrl);

            // 删除旧头像文件
            if (oldAvatar != null && !oldAvatar.isEmpty()) {
                String oldFileId = oldAvatar.substring(oldAvatar.lastIndexOf("/") + 1);
                gridFSStorageService.deleteImage(oldFileId);
            }
        }

        // 更新用户信息
        currentUser.setPhone(phone);
        currentUser.setBio(bio);

        // 修改内容保存到数据库
        userService.updateUser(currentUser);

        // 重定向到指定页面，并传递参数
        map.put("success", "个人信息更新成功");
        return ResponseEntity.ok(map);
    }

    @GetMapping("/change-password")
    /*public String changePasswordForm() {
        return "user-change-password";
    }*/
    public ResponseEntity<User> changePasswordForm() {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(user);
    }

    @PostMapping("/change-password")
    /*public String changePassword(@RequestParam String oldPassword, @RequestParam String newPassword, @RequestParam String confirmPassword, RedirectAttributes redirectAttributes) {
        // 密码验证，验证两次输入的密码是否一致
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "两次输入的密码不一致");
            return "redirect:/user/change-password";
        }

        // 密码旧密码是否正确
        if (!userService.verifyPassword(userService.getCurrentUser().getUsername(), oldPassword)) {
            redirectAttributes.addFlashAttribute("error", "旧密码错误");
            return "redirect:/user/change-password";
        }

        // 新密码强度验证
        if (!newPassword.matches("^[a-zA-Z0-9_]{8,20}$")) {
            redirectAttributes.addFlashAttribute("error", "新密码强度不够");
            return "redirect:/user/change-password";
        }

        // 更新密码到数据库
        userService.changePassword(userService.getCurrentUser().getUsername(), newPassword);
        redirectAttributes.addFlashAttribute("success", "密码修改成功");

        return "redirect:/user/change-password";
    }*/
    public ResponseEntity<?> changePassword(@RequestParam String oldPassword,
                                            @RequestParam String newPassword,
                                            @RequestParam String confirmPassword) {
        Map<String, String> map = new HashMap<>();

        // 密码验证，验证两次输入的密码是否一致
        if (!newPassword.equals(confirmPassword)) {
            map.put("error", "两次输入的密码不一致");
            return ResponseEntity.ok(map);
        }

        // 密码旧密码是否正确
        if (!userService.verifyPassword(userService.getCurrentUser().getUsername(), oldPassword)) {
            map.put("error", "旧密码错误");
            return ResponseEntity.ok(map);
        }

        // 新密码强度验证
        if (!newPassword.matches("^[a-zA-Z0-9_]{8,20}$")) {
            map.put("error", "新密码强度不够");
            return ResponseEntity.ok(map);
        }

        // 更新密码到数据库
        userService.changePassword(userService.getCurrentUser().getUsername(), newPassword);
        map.put("success", "密码修改成功");

        return ResponseEntity.ok(map);
    }

    // 获取用户笔记列表数据在界面上展示
    @GetMapping("/profile/{userId}")
    /*public String profileWithNotes(Model model,
                                   @PathVariable Long userId,
                                   @RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "12") int size) {
        // 获取当前用户信息
        Optional<User> optionalUser = userService.findByUserId(userId);

        // 判断用户是否存在
        if (!optionalUser.isPresent()) {
            throw new UserNotFoundException("");
        }

        User user = optionalUser.get();
        model.addAttribute("user", user);

        // 获取用户笔记列表数据
        Page<Note> notePage = noteService.getNotesByUser(userId, page - 1, size);

        // 添加笔记列表数据到模型中
        model.addAttribute("notePage", notePage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", notePage.getTotalPages());

        return "user-profile";
    }*/
    public ResponseEntity<?> profileWithNotes(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        // 获取当前用户信息
        Optional<User> optionalUser = userService.findByUserId(userId);

        // 判断用户是否存在
        if (!optionalUser.isPresent()) {
            throw new UserNotFoundException("");
        }

        User user = optionalUser.get();

        // 获取用户笔记列表数据
        Page<Note> notePage = noteService.getNotesByUser(userId, page - 1, size);

        // 转为DTO
        List<NoteExploreDto> noteExploreDtoList =
                notePage.map(note -> NoteExploreDto.toExploreDto(note, user)).getContent();

        // 添加笔记列表数据到模型中
        Map<String, Object> map = new HashMap<>();
        map.put("user", user);
        map.put("noteList", noteExploreDtoList);
        map.put("currentPage", page);
        map.put("totalPages", notePage.getTotalPages());

        return ResponseEntity.ok(map);
    }

}
