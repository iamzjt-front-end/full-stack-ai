package com.waylau.rednote.aimicroservice.application.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * AiCommentServiceTests AiCommentService tests
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/12/21
**/
@SpringBootTest
class AiCommentServiceTests {
    @Autowired
    private AiCommentService aiCommentService;

    // 编写测试用例
    @Test
    void testComment() {
        // 1. 构建提示词
        String prompt = aiCommentService.buildPrompt("职场", "Java在企业级AI应用开发中占有不可替代的地位。");

        // 2. 调用生成服务来生成内容
        String result = aiCommentService.generate(prompt);

        System.out.println(result);

        assertNotNull(result);
    }
}
