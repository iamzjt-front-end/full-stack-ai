package com.waylau.rednote.controller;

import com.waylau.rednote.common.LoginErrorType;
import com.waylau.rednote.dto.UserLoginDto;
import com.waylau.rednote.dto.UserRegistrationDto;
import com.waylau.rednote.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController 认证控制器
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/16
 **/
@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 显示注册表单页面
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "registration-form";
    }

    /**
     * 处理注册表单提交
     */
    @PostMapping("/register")
    public String processRegistrationForm(@Valid @ModelAttribute("user") UserRegistrationDto registrationDto,
                                          BindingResult bindingResult,
                                          Model model) {
        // 检查用户名是否已存在
        if (userService.existsByUsername(registrationDto.getUsername())) {
            bindingResult.rejectValue("username", null, "该用户名已被使用");
        }

        // 检查手机号是否已注册
        if (userService.existsByPhone(registrationDto.getPhone())) {
            bindingResult.rejectValue("phone", null, "该手机号已被注册");
        }

        // 检查手机验证码是否校验通过
        if (!userService.verifyCode(registrationDto.getPhone(), registrationDto.getVerificationCode())) {
            bindingResult.rejectValue("verificationCode", null, "验证码不正确");
        }

        // 检查用户名、手机号、验证码是否通过
        // 如果有错误，则返回注册页面
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", registrationDto);
            return "registration-form";
        }

        // 注册用户
        userService.registerUser(registrationDto);

        // 注册成功，跳转到登录页面
        return "redirect:/auth/login";
    }

    /**
     * 显示登录页面
     */
    @GetMapping("/login")
    public String showLoginForm(Model model,
                                @RequestParam(required = false) String error,
                                @Valid @ModelAttribute("user") UserLoginDto loginDto,
                                BindingResult bindingResult) {
        /*model.addAttribute("user", new UserLoginDto());*/
        model.addAttribute("user", loginDto);

        // 处理用户名未注册的错误
        if (LoginErrorType.USERNAME_NOT_FOUND.equals(error)) {
            bindingResult.rejectValue("username", null, "该用户名未注册");

            return "login-form";
        }

        // 处理密码错误
        if (LoginErrorType.INCORRECT_PASSWORD.equals(error)) {
            bindingResult.rejectValue("password", null, "密码错误");

            return "login-form";
        }

        // 处理会话失效
        if (LoginErrorType.SESSION_INVALID.equals(error)) {
            bindingResult.rejectValue("username", null, "会话失效");

            return "login-form";
        }

        // 处理会话过期
        if (LoginErrorType.SESSION_EXPIRED.equals(error)) {
            bindingResult.rejectValue("username", null, "会话过期");

            return "login-form";
        }

        // 处理用户注销
        if (LoginErrorType.LOGOUT.equals(error)) {
            bindingResult.rejectValue("username", null, "已注销");

            return "login-form";
        }

        return "login-form";
    }

    /**
     * 处理登录表单的提交
     */
    @PostMapping("/login")
    public String processLoginForm(@Valid @ModelAttribute("user") UserLoginDto loginDto,
                                   BindingResult bindingResult,
                                   Model model) {
        // 检查用户名是否存在
        if (!userService.existsByUsername(loginDto.getUsername())) {
            bindingResult.rejectValue("username", null, "该用户名未注册");

            model.addAttribute("user", loginDto);
            return "login-form";
        }

        // 检查密码是否正确
        if (!userService.verifyPassword(loginDto.getUsername(), loginDto.getPassword())) {
            bindingResult.rejectValue("password", null, "密码错误");

            model.addAttribute("user", loginDto);
            return "login-form";
        }

        // 登录成功，重定向到首页或
        return "redirect:/";
    }
}
