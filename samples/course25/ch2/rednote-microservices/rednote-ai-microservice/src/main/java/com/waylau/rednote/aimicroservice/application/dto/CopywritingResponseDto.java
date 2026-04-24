package com.waylau.rednote.aimicroservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * CopywritingResponseDto AI文案生成响应DTO
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/12/21
 **/
@Getter
@Setter
@AllArgsConstructor
public class CopywritingResponseDto {
    // 文案标题
    private String title;

    // 文案内容
    private String content;
}
