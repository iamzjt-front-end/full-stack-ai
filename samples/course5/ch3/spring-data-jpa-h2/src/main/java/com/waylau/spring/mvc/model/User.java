package com.waylau.spring.mvc.model;

import jakarta.persistence.*;

/**
 * User 用户模型
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/11
 **/
// 实体
@Entity
@Table(name = "users")
public class User {
    // 主键
    @Id
    // 自增长策略
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // 实体唯一标识
    private Long id;
    private String name;
    private String email;

    public User() {
    }

    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // Getter、Setter方法
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
