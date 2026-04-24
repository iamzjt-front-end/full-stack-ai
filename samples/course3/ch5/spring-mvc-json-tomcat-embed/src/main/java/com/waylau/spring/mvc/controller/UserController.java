package com.waylau.spring.mvc.controller;

import com.waylau.spring.mvc.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * UserController 用户控制器
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/10
 **/
@Controller
@RequestMapping("/users")
public class UserController {

    // 用户存储
    private final ConcurrentHashMap<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicLong counter = new AtomicLong(1);

    public UserController() {
        // 初始化测试数据
        Long id1 = counter.getAndIncrement();
        users.put(id1, new User(id1, "John", "john@waylau.com"));

        Long id2 = counter.getAndIncrement();
        users.put(id2, new User(id2, "Smith", "smith@waylau.com"));
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(new ArrayList<>(users.values()));
    }

    @PostMapping
    @ResponseBody
    public User createUser(@RequestBody User user) {
        Long id = counter.getAndIncrement();
        user.setId(id);

        users.put(id, user);

        return user;
    }
}
