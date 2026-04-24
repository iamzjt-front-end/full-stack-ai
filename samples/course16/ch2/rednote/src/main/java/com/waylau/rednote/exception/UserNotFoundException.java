package com.waylau.rednote.exception;

/**
 * UserNotFoundException 用户不存在异常 
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/19
**/
public class UserNotFoundException extends ValidationException{
    public UserNotFoundException(String message) {
        super("用户不存在异常. " + message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super("用户不存在异常. " + message, cause);
    }
}
