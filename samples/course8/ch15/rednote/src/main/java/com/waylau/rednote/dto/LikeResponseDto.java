package com.waylau.rednote.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * LikeResponseDto 点赞响应对象
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/21
 **/
@Getter
@Setter
@AllArgsConstructor
public class LikeResponseDto {
    /**
     * 是否点赞
     */
    private boolean isLiked;

    /**
     * 点赞数量
     */
    private long likeCount;
}
