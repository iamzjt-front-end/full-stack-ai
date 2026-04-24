package com.waylau.rednote.common.exception;

/**
 * CommentNotFoundException 评论不存在异常
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/22
 **/
public class CommentNotFoundException extends ValidationException {
    public CommentNotFoundException(String message) {
        super("评论不存在异常. " + message);
    }

    public CommentNotFoundException(String message, Throwable cause) {
        super("评论不存在异常. " + message, cause);
    }
}
