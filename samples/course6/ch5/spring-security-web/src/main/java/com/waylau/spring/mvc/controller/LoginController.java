package com.waylau.spring.mvc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * LoginController 登录控制器
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/14
 **/
@Controller
@RequestMapping("/login")
public class LoginController {

    /**
     * 用于显示登录表单
     *
     * @return
     */
    @GetMapping
    public String showLoginForm() {
        return "login-form";
    }
}
