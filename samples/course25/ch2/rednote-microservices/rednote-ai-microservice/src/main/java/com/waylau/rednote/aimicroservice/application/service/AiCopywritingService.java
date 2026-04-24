package com.waylau.rednote.aimicroservice.application.service;

/**
 * AiCopywritingService AI文案生成服务 
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/12/21
**/
public interface AiCopywritingService {
    /**
     * 生成文案
     * @param prompt
     * @return
     */
    String generate(String prompt);

    /**
     * 构建提示词（复用前文逻辑）
     * @param type
     * @param keywords
     * @return
     */
    String buildPrompt(String type, String keywords);
}
