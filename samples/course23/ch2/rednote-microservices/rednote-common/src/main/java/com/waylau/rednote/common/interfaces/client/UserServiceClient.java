package com.waylau.rednote.common.interfaces.client;

import com.waylau.rednote.common.dto.AllUsersDto;
import com.waylau.rednote.common.dto.DeleteResponseDto;
import com.waylau.rednote.common.dto.UserDto;
import com.waylau.rednote.common.dto.UserEditByAdminDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/user")
    ResponseEntity<AllUsersDto> getAllUsers(@RequestParam(defaultValue = "1") int page);

    @PostMapping("/user/edit-by-admin")
    ResponseEntity<String> updateUserByAdmin(@RequestBody UserEditByAdminDto userEditByAdminDto);

    @DeleteMapping("/user/{userId}")
    ResponseEntity<DeleteResponseDto> deleteUser(@PathVariable Long userId);

    @GetMapping("/user/count")
    ResponseEntity<Long> countUsers();
}
