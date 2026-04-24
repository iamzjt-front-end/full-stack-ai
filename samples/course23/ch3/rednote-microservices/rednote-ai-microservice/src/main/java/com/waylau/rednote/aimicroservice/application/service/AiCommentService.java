package com.waylau.rednote.aimicroservice.application.service;

/**
 * AiCommentService AI评论生成服务
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/12/22
 **/
public interface AiCommentService {
    /**
     * 生成评论建议
     *
     * @param prompt
     * @return
     */
    String generate(String prompt);

    /**
     * 构建提示词
     *
     * @param type
     * @param content
     * @return
     */
    String buildPrompt(String type, String content);
}
