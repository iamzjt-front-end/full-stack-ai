package com.waylau.rednote.common.interfaces.client;

import com.waylau.rednote.common.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * UserServiceClient UserService Feign Client
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/12/16
 **/
@FeignClient(name = "rednote-user-microservice",
        // 指定了降级实现类
        fallback = UserServiceClientFallback.class)
public interface UserServiceClient {
    @GetMapping("/user/{userId}")
    ResponseEntity<UserDto> findByUserId(@PathVariable Long userId);

    @GetMapping("/user/current")
    ResponseEntity<UserDto> getCurrentUser();

    @GetMapping("/user/username/{username}")
    ResponseEntity<UserDto> findByUsername(@PathVariable String username);
}
