package com.waylau.spring.mvc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * ErrorController 错误控制器
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/14
 **/
@Controller
@RequestMapping("/403")
public class ErrorController {

    @GetMapping
    public String accessDenied() {
        // 返回403错误页面
        return "403-error";
    }
}
