package com.waylau.rednote.service;

import com.waylau.rednote.dto.UserRegistrationDto;
import com.waylau.rednote.entity.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Optional;

/**
 * UserService 用户服务
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/16
 **/
public interface UserService {
    /**
     * 检查用户名是否已存在
     */
    boolean existsByUsername(@NotBlank(message = "用户名不能为空") @Size(min = 4, max = 20, message = "用户名长度应为4-20个字符") String username);

    /**
     * 检查手机号是否已注册
     */
    boolean existsByPhone(@NotBlank(message = "手机号不能为空") @Size(min = 11, max = 11, message = "手机号长度应为11个字符") @Pattern(regexp = "^[1][3,4,5,7,8][0-9]{8}$", message = "手机号格式不正确") String phone);

    /**
     * 验证短信验证码
     */
    boolean verifyCode(@NotBlank(message = "手机号不能为空") @Size(min = 11, max = 11, message = "手机号长度应为11个字符") @Pattern(regexp = "^[1][3,4,5,7,8][0-9]{8}$", message = "手机号格式不正确") String phone, @NotBlank(message = "验证码不能为空") @Size(min = 6, max = 6, message = "验证码长度应为6个字符") @Pattern(regexp = "^[0-9]{6}$", message = "验证码格式不正确") String verificationCode);

    /**
     * 注册新用户
     */
    void registerUser(@Valid UserRegistrationDto registrationDto);

    /**
     * 验证密码
     */
    boolean verifyPassword(@NotBlank(message = "用户名不能为空") @Size(min = 4, max = 20, message = "用户名长度应为4-20个字符") String username, @NotBlank(message = "密码不能为空") @Size(min = 8, max = 20, message = "密码长度应为8-20个字符") @Pattern(regexp = "^[a-zA-Z0-9_]{8,20}$", message = "密码格式不正确") String password);

    /**
     * 获取当前用户
     */
    User getCurrentUser();

    /**
     * 更新用户
     */
    User updateUser(User currentUser);

    /**
     * 修改密码
     */
    void changePassword(String username, String newPassword);

    /**
     * 根据用户ID查找用户
     */
    Optional<User> findByUserId(Long userId);
}
