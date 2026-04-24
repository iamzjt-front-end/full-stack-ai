package com.waylau.rednote.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * ErrorResponseDto 错误响应对象
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/09/09
 **/
@Getter
@Setter
@AllArgsConstructor
public class ErrorResponseDto {
    /**
     * HTTP状态码
     */
    private int code;

    /**
     * 信息
     */
    private String message;
}
