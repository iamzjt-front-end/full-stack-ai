package com.waylau.rednote.aimicroservice.interfaces.controller;

import com.waylau.rednote.aimicroservice.application.dto.CommentSuggestionRequestDto;
import com.waylau.rednote.aimicroservice.application.dto.CommentSuggestionResponseDto;
import com.waylau.rednote.aimicroservice.application.dto.CopywritingRequestDto;
import com.waylau.rednote.aimicroservice.application.dto.CopywritingResponseDto;
import com.waylau.rednote.aimicroservice.application.service.AiCommentService;
import com.waylau.rednote.aimicroservice.application.service.AiCopywritingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AiController AI文案生成控制器
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/12/21
 **/
@RestController
@RequestMapping("/ai")
public class AiController {
    @Autowired
    private AiCopywritingService aiCopywritingService;

    @Autowired
    private AiCommentService aiCommentService;

    @PostMapping("/copywriting")
    public ResponseEntity<CopywritingResponseDto> generateCopywriting(@RequestBody CopywritingRequestDto requestDto) {
        // 1. 构建提示词
        String prompt = aiCopywritingService.buildPrompt(requestDto.getType(), requestDto.getKeywords());

        // 2. 调用生成服务来生成内容
        String result = aiCopywritingService.generate(prompt);

        // 3. 解析为DTO，用"\n\n"分割标题和正文
        String[] textArray = result.split("\n\n");

        CopywritingResponseDto responseDto;

        // 判空处理
        if (textArray.length < 2) {
            // 默认标题为空，所有内容当作是正文
            responseDto = new CopywritingResponseDto(null, result);
        } else {
            //
            responseDto = new CopywritingResponseDto(textArray[0], textArray[1]);
        }

        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/comment")
    public ResponseEntity<CommentSuggestionResponseDto> generateComment(@RequestBody CommentSuggestionRequestDto requestDto) {
        // 1. 构建提示词
        String prompt = aiCommentService.buildPrompt(requestDto.getType(), requestDto.getContent());

        // 2. 调用生成服务来生成内容
        String result = aiCommentService.generate(prompt);

        // 3. 解析为DTO，用"\n"分割多条评论
        String[] commentArray = result.split("\n");
        CommentSuggestionResponseDto responseDto = new CommentSuggestionResponseDto(commentArray);

        return ResponseEntity.ok(responseDto);
    }
}
