package com.waylau.spring.jdbc.service;

import com.waylau.spring.jdbc.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * TxNestedServiceImpl NESTED事务服务
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2026/04/06
 **/
@Service
public class TxNestedServiceImpl implements TxService {
    @Autowired
    private UserService userService;

    // 注意！注入自己的代理对象
    @Autowired
    @Qualifier("txNestedServiceImpl")
    private TxService txService;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void outer() {
        // 创建用户
        User newUser = new User("Charlie", "charlie@waylau.com");
        userService.create(newUser);

        // 调用内层方法
        try {
            txService.inner();
        } catch (Exception e) {
            // do nothing
        }

        // 模拟异常
        //throw new RuntimeException("outer异常");
    }

    @Override
    @Transactional(propagation = Propagation.NESTED)
    public void inner() {
        // 创建用户
        User newUser = new User("Deck", "deck@waylau.com");
        userService.create(newUser);

        // 模拟异常
        throw new RuntimeException("inner异常");
    }

    @Override
    public void printAllUsers() {
        userService.findAll().stream().forEach(user -> System.out.println(user));
    }
}
