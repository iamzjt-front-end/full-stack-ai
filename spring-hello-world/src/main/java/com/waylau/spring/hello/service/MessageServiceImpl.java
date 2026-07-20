package com.waylau.spring.hello.service;

import org.springframework.stereotype.Service;

/**
 * 消息服务实现
 */
@Service
public class MessageServiceImpl implements MessageService {

    @Override
    public String getMessage() {
        return "Hello J.Tide!";
    }
}
