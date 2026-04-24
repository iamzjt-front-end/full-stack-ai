package com.waylau.rednote.exception;

/**
 * FileStorageException 文件存储异常
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/18
 **/
public class FileStorageException extends ValidationException {
    public FileStorageException(String message) {
        super("文件存储异常. " + message);
    }

    public FileStorageException(String message, Throwable cause) {
        super("文件存储异常. " + message, cause);
    }
}
