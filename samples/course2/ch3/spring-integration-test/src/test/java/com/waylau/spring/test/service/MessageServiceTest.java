package com.waylau.spring.test.service;

import com.waylau.spring.test.App;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * MessageServiceTest MessageService test
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/09
 **/
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = App.class)
public class MessageServiceTest {

    @Autowired
    private MessageService service;

    @Test
    void testGetMessage() {
        assertEquals("Hello World!", service.getMessage());
    }

}
