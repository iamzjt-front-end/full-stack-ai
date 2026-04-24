package com.waylau.rednote.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * IndexController 首页控制器
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/20
 **/
@Controller
@RequestMapping("/")
public class IndexController {
    @GetMapping
    public String index() {
        // 重定向到首页笔记探索页面
        return "redirect:/explore";
    }
}
