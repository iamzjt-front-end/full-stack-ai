package com.waylau.rednote.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * BaseSecurityConfig 各微服务共有的安全配置 
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/12/18
**/
public abstract class BaseSecurityConfig {
    // 注册 BCryptPasswordEncoder Bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 构造函数可以指定密码的强度，默认是10
        return new BCryptPasswordEncoder(10);
    }

    // JWT认证过滤器
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    // 公共配置：无状态会话、禁用CSRF等所有微服务通用的配置
    protected  void configureCommon(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF防护
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        // 允许指定资源的请求不需要认证
                        .requestMatchers("/auth/register", "/auth/login", "/css/**", "/js/**", "/fonts/**", "/images/**", "/favicon.ico").permitAll()
                        .requestMatchers("/error/**").permitAll()
                        // 允许ADMIN角色的用户访问 /admin/** 的资源
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // 允许ADMIN、USER角色的用户访问 /user/** 的资源
                        .requestMatchers("/user/**").hasAnyRole("ADMIN", "USER")
                        // 允许USER角色的用户访问 /note/** 的资源
                        .requestMatchers("/note/**").hasRole("USER")
                        // 允许USER角色的用户访问 /explore/** 的资源
                        .requestMatchers("/explore/**").hasRole("USER")
                        // 允许USER角色的用户访问 /like/** 的资源
                        .requestMatchers("/like/**").hasRole("USER")
                        // 允许USER角色的用户访问 /comment/** 的资源
                        .requestMatchers("/comment/**").hasRole("USER")
                        // 允许USER角色的用户访问 /log/** 的资源
                        .requestMatchers("/log/**").hasRole("USER")
                        // 允许匿名访问静态图片资源
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/file/**").permitAll()
                        // 其他请求需求认证
                        .anyRequest().authenticated()
                )
                // 异常处理
                .exceptionHandling(exception -> exception
                        // 指定403错误页面
                        .accessDeniedPage("/error/403")
                )
                // 无状态会话
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 启用JWT认证过滤器
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
        ;
    }

    // 抽象方法：留给子类实现服务特定的权限规则
    protected abstract void configureAuthorize(HttpSecurity http) throws Exception;
}
