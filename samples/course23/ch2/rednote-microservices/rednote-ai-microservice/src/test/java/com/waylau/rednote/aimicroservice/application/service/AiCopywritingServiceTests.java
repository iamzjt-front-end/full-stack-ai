package com.waylau.rednote.aimicroservice.application.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * AiCopywritingServiceTests AiCopywritingService tests 
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/12/21
**/
@SpringBootTest
class AiCopywritingServiceTests {
    @Autowired
    private AiCopywritingService aiCopywritingService;

    // 编写测试用例
    @Test
    void testCopywriting() {
        // 1. 构建提示词
        String prompt = aiCopywritingService.buildPrompt("职场", "Java、开发、编程");

        // 2. 调用生成服务来生成内容
        String result = aiCopywritingService.generate(prompt);

        System.out.println(result);

        assertNotNull(result);
    }
}
