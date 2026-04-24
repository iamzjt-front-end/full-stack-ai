package com.waylau.rednote.common.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * FeignTokenInterceptor Feign Token Interceptor 
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/12/18
**/
@Component
public class FeignTokenInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 适配UsernamePasswordAuthenticationToken
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            // 从 RequestAttributes 获取令牌
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                String token = (String)attributes.getAttribute("JWT_TOKEN", RequestAttributes.SCOPE_REQUEST);
                if (token != null) {
                    requestTemplate.header("Authorization", "Bearer " + token);
                }
            }
        }
    }
}
