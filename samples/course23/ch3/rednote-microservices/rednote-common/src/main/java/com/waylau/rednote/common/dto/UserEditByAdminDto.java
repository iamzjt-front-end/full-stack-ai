package com.waylau.rednote.common.dto;

import com.waylau.rednote.common.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

/**
 * UserEditByAdminDto
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/12/18
 **/
@Getter
@Setter
public class UserEditByAdminDto {
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
     * 密码
     */
    private String password;

    /**
     * 角色
     */
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;
}
