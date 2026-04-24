package com.waylau.rednote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * UserLoginDto 用户登录DTO
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/16
 **/
@Getter
@Setter
public class UserLoginDto {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度应为4-20个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度应为8-20个字符")
    @Pattern(regexp = "^[a-zA-Z0-9_]{8,20}$", message = "密码格式不正确")
    private String password;

    private boolean rememberMe;
}
