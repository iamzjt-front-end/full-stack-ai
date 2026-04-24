package com.waylau.rednote.common.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * CustomUserDetails 自定义UserDetails
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/21
 **/
public class CustomUserDetails extends User {
    // 新增用户ID字段
    private final Long userId;

    public CustomUserDetails(Long userId,
                             String username,
                             String password,
                             Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);

        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }
}
