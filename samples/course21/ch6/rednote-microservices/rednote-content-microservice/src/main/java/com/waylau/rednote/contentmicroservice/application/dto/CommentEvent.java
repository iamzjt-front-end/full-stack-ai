package com.waylau.rednote.contentmicroservice.application.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * CommentEvent 评论事件
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/09/05
 **/
@Getter
@Setter
public class CommentEvent extends UserActionEvent {
    /**
     * 笔记ID
     */
    private Long noteId;
    /**
     * 评论ID
     */
    private Long commentId;
    /**
     * 评论内容
     */
    private String content;
}
