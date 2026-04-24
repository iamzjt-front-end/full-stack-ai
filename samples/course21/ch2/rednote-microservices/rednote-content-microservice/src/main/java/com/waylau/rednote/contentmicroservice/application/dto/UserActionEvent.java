package com.waylau.rednote.contentmicroservice.application.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * UserActionEvent 行为日志的基类
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/09/05
 **/
@Getter
@Setter
public abstract class UserActionEvent {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 时间戳
     */
    private LocalDateTime timestamp;
}
