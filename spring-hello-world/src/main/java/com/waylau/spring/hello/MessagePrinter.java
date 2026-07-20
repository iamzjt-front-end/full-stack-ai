package com.waylau.spring.hello;

import com.waylau.spring.hello.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 打印器
 */
@Component
public class MessagePrinter {
    final private MessageService service;

    @Autowired
    public MessagePrinter(MessageService service) {
        this.service  = service;
    }

    public void printMessage(){
        String message = this.service.getMessage();
        System.out.println(message);
    }
}
