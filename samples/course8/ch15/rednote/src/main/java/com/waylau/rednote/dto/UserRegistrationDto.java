package com.waylau.rednote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * UserRegistrationDto 用户注册DTO
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/16
 **/
@Getter
@Setter
public class UserRegistrationDto {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度应为4-20个字符")
    private String username;

    @NotBlank(message = "手机号不能为空")
    @Size(min = 11, max = 11, message = "手机号长度应为11个字符")
    @Pattern(regexp = "^[1][3,4,5,7,8][0-9]{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    @Size(min = 6, max = 6, message = "验证码长度应为6个字符")
    @Pattern(regexp = "^[0-9]{6}$", message = "验证码格式不正确")
    private String verificationCode;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度应为8-20个字符")
    @Pattern(regexp = "^[a-zA-Z0-9_]{8,20}$", message = "密码格式不正确")
    private String password;
}
