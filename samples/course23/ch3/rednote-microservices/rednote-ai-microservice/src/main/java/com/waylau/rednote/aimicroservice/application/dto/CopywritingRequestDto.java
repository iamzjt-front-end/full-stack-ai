package com.waylau.rednote.aimicroservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * CopywritingRequestDto AI文案生成请求DTO
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/12/21
 **/
@Getter
@Setter
@AllArgsConstructor
public class CopywritingRequestDto {
    // 内容类型
    private String type;
    // 用户输入的关键词
    private String keywords;
}
