package com.waylau.rednote.usermicroservice.interfaces.controller;

import com.waylau.rednote.common.constant.FileConstant;
import com.waylau.rednote.common.dto.*;
import com.waylau.rednote.common.interfaces.client.ContentServiceClient;
import com.waylau.rednote.common.interfaces.client.FileServiceClient;
import com.waylau.rednote.usermicroservice.domain.model.entity.User;
import com.waylau.rednote.common.exception.UserNotFoundException;
import com.waylau.rednote.usermicroservice.application.service.UserService;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private static final int PAGE_SIZE = 10;

    @Autowired
    private UserService userService;

    @Autowired
    //private GridFSStorageService gridFSStorageService;
    private FileServiceClient fileServiceClient;

    @Autowired
    //private NoteService noteService;
    private ContentServiceClient contentServiceClient;

    @GetMapping("/profile")
    public ResponseEntity<User> profile() {
        // 获取当前用户信息
        User user = userService.getCurrentUser();

        return ResponseEntity.ok(user);
    }

    @GetMapping("/edit")
    public ResponseEntity<User> editProfile() {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(user);
    }

    /*@Transactional*/
    @GlobalTransactional
    @PostMapping("/edit")
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
            /*String fileId = gridFSStorageService.uploadImage(avatarFile);
            String fileUrl = MongoConfig.STATIC_PATH_PREFIX + fileId;

            currentUser.setAvatar(fileUrl);

            // 删除旧头像文件
            if (oldAvatar != null && !oldAvatar.isEmpty()) {
                String oldFileId = oldAvatar.substring(oldAvatar.lastIndexOf("/") + 1);
                gridFSStorageService.deleteImage(oldFileId);
            }*/
            String fileId = fileServiceClient.uploadImage(avatarFile).getBody().toString();
            String fileUrl = FileConstant.STATIC_PATH_PREFIX + fileId;

            currentUser.setAvatar(fileUrl);

            // 删除旧头像文件
            if (oldAvatar != null && !oldAvatar.isEmpty()) {
                String oldFileId = oldAvatar.substring(FileConstant.STATIC_PATH_PREFIX.length());
                fileServiceClient.deleteImage(oldFileId);
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
    public ResponseEntity<User> changePasswordForm() {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(user);
    }

    @PostMapping("/change-password")
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

        /*User user = optionalUser.get();

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
        map.put("totalPages", notePage.getTotalPages());*/
        ResponseEntity<NotesWithUserDto> notesWithUser
                = contentServiceClient.getNotesByUser(userId, page, size);
        NotesWithUserDto notesWithUserDto = notesWithUser.getBody();

        return ResponseEntity.ok(notesWithUserDto);
    }

    @GetMapping("/{userId}")
    ResponseEntity<UserDto> findByUserId(@PathVariable Long userId) {
        Optional<User> userOptional = userService.findByUserId(userId);

        if (!userOptional.isPresent()) {
            throw new UserNotFoundException("");
        }

        User user = userOptional.get();

        // 转为DTO
        UserDto userDto = new UserDto();
        userDto.setUserId(user.getUserId());
        userDto.setUsername(user.getUsername());
        userDto.setPhone(user.getPhone());
        userDto.setAvatar(user.getAvatar());
        userDto.setBio(user.getBio());
        userDto.setRole(user.getRole());

        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/current")
    ResponseEntity<UserDto> getCurrentUser() {
        User user = userService.getCurrentUser();

        // 转为DTO
        UserDto userDto = new UserDto();
        userDto.setUserId(user.getUserId());
        userDto.setUsername(user.getUsername());
        userDto.setPhone(user.getPhone());
        userDto.setAvatar(user.getAvatar());
        userDto.setBio(user.getBio());
        userDto.setRole(user.getRole());

        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/username/{username}")
    ResponseEntity<UserDto> findByUsername(@PathVariable String username) {
        Optional<User> userOptional = userService.findByUsername(username);

        if (!userOptional.isPresent()) {
            throw new UserNotFoundException("");
        }

        User user = userOptional.get();

        // 转为DTO
        UserDto userDto = new UserDto();
        userDto.setUserId(user.getUserId());
        userDto.setUsername(user.getUsername());
        userDto.setPhone(user.getPhone());
        userDto.setAvatar(user.getAvatar());
        userDto.setBio(user.getBio());
        userDto.setRole(user.getRole());

        return ResponseEntity.ok(userDto);
    }

    @GetMapping
    ResponseEntity<AllUsersDto> getAllUsers(@RequestParam(defaultValue = "1") int page) {
        // 分页查询所有用户数据
        Page<User> userPage = userService.getAllUsers(page, PAGE_SIZE);

        // 转为为DTO
        List<UserEditByAdminDto> userDtoList = userPage.map(user -> {
            UserEditByAdminDto userDto = new UserEditByAdminDto();
            userDto.setUserId(user.getUserId());
            userDto.setUsername(user.getUsername());
            userDto.setPhone(user.getPhone());
            userDto.setRole(user.getRole());

            return userDto;
        }).getContent();

        AllUsersDto allUsersDto = new AllUsersDto(userDtoList, page, userPage.getTotalPages());

        return ResponseEntity.ok(allUsersDto);
    }

    @PostMapping("/edit-by-admin")
    ResponseEntity<String> updateUserByAdmin(@RequestBody UserEditByAdminDto user) {
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

    @DeleteMapping("/{userId}")
    ResponseEntity<DeleteResponseDto> deleteUser(@PathVariable Long userId) {
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

    @GetMapping("/count")
    ResponseEntity<Long> countUsers() {
        long userCount = userService.countUsers();
        return ResponseEntity.ok(userCount);
    }
}
