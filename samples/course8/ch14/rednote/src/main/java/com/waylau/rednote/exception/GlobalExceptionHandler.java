package com.waylau.rednote.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;


/**
 * GlobalExceptionHandler 全局异常处理
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/18
 **/
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException exc, Model model) {
        log.error("服务器异常：{}", exc.getMessage(), exc);

        model.addAttribute("errorCode", 400);
        model.addAttribute("errorMessage", "服务器异常：" + exc.getMessage());

        return "400-error";
    }

    // 用户不存在异常
    @ExceptionHandler(UserNotFoundException.class)
    public String handleUserNotFoundException(UserNotFoundException exc, Model model) {
        log.error("用户不存在异常：{}", exc.getMessage(), exc);

        model.addAttribute("errorCode", 404);
        model.addAttribute("errorMessage", "异常信息：" + exc.getMessage());

        return "400-error";
    }

    // 笔记不存在异常
    @ExceptionHandler(NoteNotFoundException.class)
    public String handleNoteNotFoundException(NoteNotFoundException exc, Model model) {
        log.error("笔记不存在异常：{}", exc.getMessage(), exc);

        model.addAttribute("errorCode", 404);
        model.addAttribute("errorMessage", "异常信息：" + exc.getMessage());

        return "400-error";
    }

    // 评论不存在异常
    @ExceptionHandler(CommentNotFoundException.class)
    public String handleCommentNotFoundException(CommentNotFoundException exc, Model model) {
        log.error("评论不存在异常：{}", exc.getMessage(), exc);

        model.addAttribute("errorCode", 404);
        model.addAttribute("errorMessage", "异常信息：" + exc.getMessage());

        return "400-error";
    }
}
