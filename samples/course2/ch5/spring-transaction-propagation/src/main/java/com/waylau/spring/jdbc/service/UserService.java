package com.waylau.spring.jdbc.service;

import com.waylau.spring.jdbc.entity.User;

import java.util.List;

/**
 * UserService 用户服务
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2026/04/06
 **/
public interface UserService {
    User create(User user);

    List<User> findAll();
}
