package com.waylau.spring.di;

import com.waylau.spring.di.service.MessageService;

/**
 * MessagePrinter 打印器
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/09
 **/
public class MessagePrinter {
    final private MessageService service;

    public MessagePrinter(MessageService service) {
        this.service = service;
    }

    public void printMessage() {
        String message = this.service.getMessage();
        System.out.println(message);
    }
}
