package com.waylau.spring.test.service;

import org.springframework.stereotype.Service;

/**
 * MessagServiceImpl 消息服务
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/09
 **/
@Service
public class MessageServiceImpl implements MessageService {
    @Override
    public String getMessage() {
        return "Hello World!";
    }
}
