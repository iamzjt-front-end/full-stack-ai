package com.waylau.rednote.config;

import com.waylau.rednote.common.ExceptionType;
import com.waylau.rednote.common.LoginErrorType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

/**
 * CustomAuthenticationFailureHandler 自定义的AuthenticationFailureHandler
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/17
 **/
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationFailureHandler.class);

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        // 添加自定义的逻辑，比如日志记录、设置错误消息等
        String exceptionMessage = exception.getMessage();
        log.error("Authentication failed: {}", exceptionMessage);

        if (exceptionMessage.contains(ExceptionType.USERNAME_NOT_FOUND)) {
            // 处理用户名不存在的情况
            response.sendRedirect("/auth/login?error=" + LoginErrorType.USERNAME_NOT_FOUND);
        } else if (exceptionMessage.contains(ExceptionType.INCORRECT_PASSWORD)) {
            // 处理密码错误情况
            response.sendRedirect("/auth/login?error=" + LoginErrorType.INCORRECT_PASSWORD);
        } else {
            // 处理其他异常情况
            response.sendRedirect("/auth/login?error=unknown");
        }
    }
}
