package com.waylau.rednote.aimicroservice.application.service;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

/**
 * AiCopywritingServiceImpl AI文案生成服务
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/12/21
 **/
@Service
public class AiCopywritingServiceImpl implements AiCopywritingService {
    // 注入Spring AI统一的ChatClient
    private final ChatClient chatClient;

    // 构造注入（无需手动创建客户端）
    public AiCopywritingServiceImpl(ChatModel chatModel) {
        // 构造时，可以设置ChatClient的参数
        this.chatClient = ChatClient.builder(chatModel)
                // 实现Logger的Advisor
                .defaultAdvisors(new SimpleLoggerAdvisor())
                // 设置ChatClient中ChatModel的Options参数
                .defaultOptions(
                        DashScopeChatOptions.builder()
                                .withTopP(0.7)
                                .build()
                )
                .build();
    }

    @Override
    public String generate(String prompt) {
        // 1. 构造提示词
        Prompt aiPrompt = new Prompt(prompt);

        // 2. 调用AI模型
        ChatResponse response = chatClient.prompt(aiPrompt).call().chatResponse();

        // 3. 提取生成结果
        return response.getResult().getOutput().getText();
    }

    @Override
    public String buildPrompt(String type, String keywords) {
        return "请生成一篇小红书风格的" + type + "笔记文案，要求：" +
                "1. 标题要吸引人，带emoji；" +
                "2. 正文分3-4行，每句不超过20字" +
                "3. 结合关键字：" + keywords;
    }
}
