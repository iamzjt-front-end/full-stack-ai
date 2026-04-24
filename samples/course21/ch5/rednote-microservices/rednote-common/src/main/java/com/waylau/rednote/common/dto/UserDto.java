package com.waylau.rednote.common.dto;

import com.waylau.rednote.common.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

/**
 * UserDto User DTO
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/12/16
 **/
@Getter
@Setter
public class UserDto {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 简介
     */
    private String bio;

    /**
     * 角色
     */
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

}
