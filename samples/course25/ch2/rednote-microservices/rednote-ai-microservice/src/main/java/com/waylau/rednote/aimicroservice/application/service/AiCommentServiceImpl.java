package com.waylau.rednote.aimicroservice.application.service;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

/**
 * AiCommentServiceImpl AI评论生成服务
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/12/22
 **/
@Service
public class AiCommentServiceImpl implements AiCommentService {
    // 注入Spring AI的ChatClient
    private final ChatClient chatClient;

    public AiCommentServiceImpl(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel)
                //实现Logger的Advisor
                .defaultAdvisors(
                        new SimpleLoggerAdvisor()
                )
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
    public String buildPrompt(String type, String content) {
        return "请为以下笔记生成" + type + "风格的评论建议，要求：" +
                "1. 笔记内容为：" + content + "。" +
                "2. 每条评论不超过20字，带emoji，符合年轻人语气，不要重复" +
                "3. 提供共3-5条评论。除了评论内容外，不要有其他多余内容。评论条目前面不要加序号";
    }
}
