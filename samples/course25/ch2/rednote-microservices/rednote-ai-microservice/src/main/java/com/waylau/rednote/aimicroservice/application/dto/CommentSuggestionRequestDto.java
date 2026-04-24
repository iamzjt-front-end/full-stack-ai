package com.waylau.rednote.aimicroservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * CommentSuggestionRequestDto AI评论生成请求DTO
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/12/21
 **/
@Getter
@Setter
@AllArgsConstructor
public class CommentSuggestionRequestDto {
    // 内容类型
    private String type;
    // 内容
    private String content;
}
