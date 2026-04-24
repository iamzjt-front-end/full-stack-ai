package com.waylau.spring.hello.service;

import org.springframework.stereotype.Service;

/**
 * MessageServiceImpl 消息服务
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/08
 **/
@Service
public class MessageServiceImpl implements MessageService {
    @Override
    public String getMessage() {
        return "Hello World!";
    }
}
