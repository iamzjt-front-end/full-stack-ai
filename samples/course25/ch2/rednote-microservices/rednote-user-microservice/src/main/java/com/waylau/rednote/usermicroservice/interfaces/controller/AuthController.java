package com.waylau.rednote.usermicroservice.interfaces.controller;

import com.waylau.rednote.common.LoginErrorType;
import com.waylau.rednote.common.config.CustomUserDetails;
import com.waylau.rednote.common.config.JwtTokenProvider;
import com.waylau.rednote.usermicroservice.application.dto.UserLoginDto;
import com.waylau.rednote.usermicroservice.application.dto.UserRegistrationDto;
import com.waylau.rednote.usermicroservice.application.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;
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
    public ResponseEntity<?> processRegistrationForm(@Valid @RequestBody UserRegistrationDto registrationDto,
                                                     BindingResult bindingResult) {
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
        // 如果有错误，则返回错误列表
        if (bindingResult.hasErrors()) {
            // 自定义错误响应
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );

            return ResponseEntity.badRequest().body(errors);
        }

        // 注册用户
        userService.registerUser(registrationDto);

        // 注册成功
        return ResponseEntity.ok("用户注册成功");
    }

    /**
     * 显示登录页面
     */
    @GetMapping("/login")
    public String showLoginForm(Model model,
                                @RequestParam(required = false) String error,
                                @Valid @ModelAttribute("user") UserLoginDto loginDto,
                                BindingResult bindingResult) {
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
    public ResponseEntity<?> processLoginForm(@Valid @RequestBody UserLoginDto loginDto,
                                   BindingResult bindingResult) {
        // 检查用户名是否存在
        if (!userService.existsByUsername(loginDto.getUsername())) {
            bindingResult.rejectValue("username", null, "该用户名未注册");

            // 自定义错误响应
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }

        // 检查密码是否正确
        if (!userService.verifyPassword(loginDto.getUsername(), loginDto.getPassword())) {
            bindingResult.rejectValue("password", null, "密码错误");

            // 自定义错误响应
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }

        /*// 获取认证用户
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getUsername(),
                        loginDto.getPassword()
                )
        );

        // 校验成功，生成JWT
        String jwt = jwtTokenProvider.generateToken(authentication);*/

        // 生成JWT
        // 1. 获取用户信息（从数据库获取）
        CustomUserDetails userDetails = (CustomUserDetails)userDetailsService.loadUserByUsername(loginDto.getUsername());

        // 2. 生成JWT（包含用户ID、用户名、权限等信息）
        String jwt = jwtTokenProvider.generateToken(userDetails);

        // 返回响应
        return ResponseEntity.ok(jwt);
    }
}
