package com.waylau.rednote.common.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;

/**
 * JwtAuthenticationFilter JWT认证过滤器
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/09/08
 **/
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 从请求头中获取JWT
        String jwt = getJwtFromRequest(request);
        if (jwt != null && jwtTokenProvider.validateJwtToken(jwt)) {
            // 从JWT中获取用户名
            String username = jwtTokenProvider.getUsernameFromJwtToken(jwt);

            // 从用户名中获取用户详情
            /*UserDetails userDetails = userDetailsService.loadUserByUsername(username);*/

            // 1. 从JWT中获取用户信息
            // 新增：解析用户ID
            Long userId = jwtTokenProvider.getUserIdFromJwtToken(jwt);
            // 新增：解析权限
            Collection<? extends GrantedAuthority> authorities = jwtTokenProvider.getAuthoritiesFromJwtToken(jwt);

            // 2. 构建用户信息（自定义 UserDetails 实现）
            UserDetails userDetails = new CustomUserDetails(userId, username, "", authorities);

            // 创建一个已认证的Authentication对象
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 设置已认证的Authentication对象到SecurityContextHolder中
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 3. 关键：存储token到请求上下文
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                attributes.setAttribute("JWT_TOKEN", jwt, RequestAttributes.SCOPE_REQUEST);

                // 可临时打印日志验证存储是否成功
                logger.info("存储JWT_TOKEN到上下文");
            } else {
                logger.error("RequestAttributes为空，无法存储JWT_TOKEN");
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中获取JWT
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
