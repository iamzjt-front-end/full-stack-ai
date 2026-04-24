package com.waylau.rednote.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static com.waylau.rednote.common.LoginErrorType.*;

/**
 * WebSecurityConfig 安全配置
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/16
 **/
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    // 添加安全过滤器链
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 启用CSRF防护
                .csrf(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize
                        // 允许指定资源的请求不需要认证
                        .requestMatchers("/auth/register", "/auth/login", "/css/**", "/js/**", "/fonts/**", "/images/**", "/favicon.ico").permitAll()
                        .requestMatchers("/error/**").permitAll()
                        // 允许ADMIN角色的用户访问 /admin/** 的资源
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // 允许ADMIN、USER角色的用户访问 /user/** 的资源
                        .requestMatchers("/user/**").hasAnyRole("ADMIN", "USER")
                        // 其他请求需求认证
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        // 指定登录页面
                        .loginPage("/auth/login")
                        // 指定执行登录的地址
                        .loginProcessingUrl("/auth/login")
                        // 自定义失败处理器
                        .failureHandler(authenticationFailureHandler())
                        // 指定登录成功后跳转的页面
                        .defaultSuccessUrl("/")
                        .permitAll()
                )
                // 异常处理
                .exceptionHandling(exception -> exception
                        // 指定403错误页面
                        .accessDeniedPage("/error/403")
                )
                // 会话管理
                .sessionManagement(session -> session
                        // 会话创建策略
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        // 访问无效会话时，重定向到指定URL
                        .invalidSessionUrl("/auth/login?error=" + SESSION_INVALID)
                        // 同一用户最大会话数
                        .maximumSessions(1)
                        // 访问过期会话时，重定向到指定URL
                        .expiredUrl("/auth/login?error=" + SESSION_EXPIRED)
                        // false表示允许新登录，踢掉旧会话，旧会话会过期
                        .maxSessionsPreventsLogin(false)
                        // 会话注册表
                        .sessionRegistry(sessionRegistry())
                )
                // 注销
                .logout(logout -> logout
                        // 清理会话
                        .invalidateHttpSession( true)
                        // 清理认证信息
                        .clearAuthentication(true)
                        // 用户访问此URL时，交由Spring Security处理注销逻辑
                        .logoutUrl("/logout")
                        // 注销成功后，重定向到指定URL
                        .logoutSuccessUrl("/auth/login?error=" + LOGOUT)
                        // 删除会话Cookie
                        .deleteCookies("JSESSIONID")

                )
                // 记住我
                .rememberMe(rememberMe -> rememberMe
                        // 设置记住我令牌的有效期（秒），默认是2周。以下设置1周
                        .tokenValiditySeconds(60 * 60 * 24 * 7)
                        // 设置用于签名令牌的密钥
                        .key("rnRememberMeKey")
                )
        ;

        return http.build();
    }

    // 注册 BCryptPasswordEncoder Bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 构造函数可以指定密码的强度，默认是10
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public CustomAuthenticationFailureHandler authenticationFailureHandler() {
        return new CustomAuthenticationFailureHandler();
    }

    @Bean
    public CustomAuthenticationProvider authenticationProvider() {
        return new CustomAuthenticationProvider(userDetailsService, passwordEncoder());
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }
}
