package com.waylau.spring.di.service;

/**
 * MessageServiceImpl 消息服务
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/09
 **/
public class MessageServiceImpl implements MessageService {
    private String username;
    private String position;

    public MessageServiceImpl(String username, String position) {
        this.username = username;
        this.position = position;
    }

    @Override
    public String getMessage() {
        return "I am " + this.username +
                ", and my position in the company is as a " + this.position;
    }
}
