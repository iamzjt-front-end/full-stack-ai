package com.waylau.rednote.controller;

import com.waylau.rednote.common.Role;
import com.waylau.rednote.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private UserService userService;

    @GetMapping
    public String index() {
        /*// 重定向到首页笔记探索页面
        return "redirect:/explore";*/

        // 判定当前用户角色，如果是管理员则跳转到管理页面，否则跳转到笔记探索页面
        return "redirect:/" + (userService.getCurrentUser().getRole() == Role.ADMIN ? "admin" : "explore");
    }
}
