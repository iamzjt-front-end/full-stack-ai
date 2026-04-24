package com.waylau.spring.jdbc.service;

import com.waylau.spring.jdbc.dao.UserDao;
import com.waylau.spring.jdbc.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * UserServiceImpl 用户服务
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2026/04/06
 **/
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;

    @Override
    public User create(User user) {
        return userDao.create(user);
    }

    @Override
    public List<User> findAll() {
        return userDao.findAll();
    }
}
