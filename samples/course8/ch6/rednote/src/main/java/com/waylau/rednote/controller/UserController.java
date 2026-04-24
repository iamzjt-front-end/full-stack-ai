package com.waylau.rednote.controller;

import com.waylau.rednote.entity.User;
import com.waylau.rednote.service.FileStorageService;
import com.waylau.rednote.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

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

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/profile")
    public String profile(Model model) {
        // 获取当前用户信息
        User user = userService.getCurrentUser();

        model.addAttribute("user", user);

        return "user-profile";
    }

    @GetMapping("/edit")
    public String editProfile(Model model) {
        User user = userService.getCurrentUser();

        model.addAttribute("user", user);

        return "user-profile-edit";
    }


    @PostMapping("/edit")
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

            // 文件名
            String fileName = avatarFile.getOriginalFilename();

            // 处理文件上传
            String fileUrl = fileStorageService.saveFile(avatarFile, fileName);
            currentUser.setAvatar(fileUrl);

            // 删除旧头像文件
            fileStorageService.deleteFile(oldAvatar);
        }

        // 更新用户信息
        currentUser.setPhone(user.getPhone());
        /*currentUser.setAvatar(user.getAvatar());*/
        currentUser.setBio(user.getBio());

        // 修改内容保存到数据库
        userService.updateUser(currentUser);

        // 重定向到指定页面，并传递参数
        redirectAttributes.addFlashAttribute("success", "个人信息更新成功");

        return "redirect:/user/profile";
    }

    @GetMapping("/change-password")
    public String changePasswordForm() {
        return "user-change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String oldPassword, @RequestParam String newPassword, @RequestParam String confirmPassword, RedirectAttributes redirectAttributes) {
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
    }

}
