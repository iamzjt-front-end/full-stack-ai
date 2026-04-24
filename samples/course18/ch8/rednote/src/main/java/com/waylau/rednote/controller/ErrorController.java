package com.waylau.rednote.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * ErrorController 错误控制器
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/17
 **/
@Controller
@RequestMapping("/error")
public class ErrorController {
    /**
     * 返回403错误页面
     */
    @GetMapping("/403")
    public String accessDenied() {
        return "403-error";
    }
}
