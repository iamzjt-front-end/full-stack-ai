package com.waylau.rednote.exception;

/**
 * NoteNotFoundException 笔记不存在异常
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/19
 **/
public class NoteNotFoundException extends ValidationException {
    public NoteNotFoundException(String message) {
        super("笔记不存在异常. " + message);
    }

    public NoteNotFoundException(String message, Throwable cause) {
        super("笔记不存在异常. " + message, cause);
    }
}
