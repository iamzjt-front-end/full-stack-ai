package com.waylau.rednote.contentmicroservice.application.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * CommentResponseDto 评论的响应对象
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/22
 **/
@Getter
@Setter
public class CommentResponseDto {

    // 以下是从Comment对象中获取
    private Long commentId;
    private String content;
    private LocalDateTime createAt;

    // 以下是从User对象中获取
    private Long userId;
    private String username;
    private String avatar;

    // 以下是从Note对象中获取
    private Long noteId;

    // 以下是从Comment的parent对象中获取
    private Long parentCommentId;

    // 以下是从Comment的parent对象中的User对象中获取
    private String parentCommentUsername;

    // 子评论
    private List<CommentResponseDto> replies = new ArrayList<>();

}
