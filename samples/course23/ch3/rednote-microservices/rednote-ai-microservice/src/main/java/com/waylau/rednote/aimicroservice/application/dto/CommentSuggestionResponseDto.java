package com.waylau.rednote.aimicroservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * CommentSuggestionResponseDto AI评论生成响应DTO
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/12/21
 **/
@Getter
@Setter
@AllArgsConstructor
public class CommentSuggestionResponseDto {
    // 评论建议数组
    private String[] commentSuggestions;
}
