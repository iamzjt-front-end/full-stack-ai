package com.waylau.rednote.common.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DeleteResponseDto 执行删除的响应对象 
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/20
**/
@Getter
@Setter
public class DeleteResponseDto {
    /**
     * 提示信息
     */
    private String message;

    /**
     * 重定向的URL
     */
    private String redirectUrl;
}
