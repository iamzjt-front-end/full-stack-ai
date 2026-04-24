package com.waylau.spring.mvc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * WebSecurityConfig 安全配置
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/14
 **/
@Configuration
// 启用Spring Security安全配置功能
@EnableWebSecurity
public class WebSecurityConfig {
    // 在内存中存储认证用户信息
    @Bean
    public UserDetailsService userDetailsService() {
        // 初始化2个认证用户信息，分别代表两个角色
        User.UserBuilder users = User.builder();
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(users.username("waylau").password("{noop}123456").roles("USER").build());
        manager.createUser(users.username("admin").password("{noop}admin123").roles("ADMIN").build());

        return manager;
    }

    // 配置安全过滤器链
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // 允许访问登录界面和静态资源
                        .requestMatchers("/login", "/css/**", "/js/**", "/fonts/**", "/images/**").permitAll()
                        // 允许USER和ADMIN角色访问
                        .requestMatchers("/admin", "/admin/dashborad").hasAnyRole("USER", "ADMIN")
                        // 允许ADMIN角色访问
                        .requestMatchers("/admin/user").hasRole("ADMIN")
                        // 其他请求需要认证
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                /*.formLogin(Customizer.withDefaults())*/
                .formLogin(form -> form
                        // 登录表单的页面
                        .loginPage("/login")
                        // 自定义执行登录的地址
                        .loginProcessingUrl("/login")
                        // 登录成功后调整的页面
                        .defaultSuccessUrl("/admin")
                        .permitAll()
                )
                // 异常处理
                .exceptionHandling(exception -> exception
                        // 指定403错误页面
                        .accessDeniedPage("/403")
                )
                // 注销
                .logout(logout -> logout
                        // 清除会话
                        .invalidateHttpSession(true)
                        // 清除认证信息
                        .clearAuthentication(true)
                        // 用户访问此URL时，交由Spring Security自动处理退出逻辑
                        .logoutUrl("/logout")
                        // 注销成功后跳转的URL
                        .logoutSuccessUrl("/login")
                        // 删除会话Cookie
                        .deleteCookies("JSESSIONID")
                )
        ;

        return http.build();
    }
}
