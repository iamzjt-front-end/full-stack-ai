package com.waylau.rednote.common;

/**
 * Role 角色枚举
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/23
 **/
public enum Role {
    USER("用户"),
    ADMIN("管理员");

    private final String description;

    Role(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
