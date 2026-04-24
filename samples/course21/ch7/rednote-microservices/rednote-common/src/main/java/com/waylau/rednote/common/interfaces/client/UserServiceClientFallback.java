package com.waylau.rednote.common.interfaces.client;

import com.waylau.rednote.common.dto.AllUsersDto;
import com.waylau.rednote.common.dto.DeleteResponseDto;
import com.waylau.rednote.common.dto.UserDto;
import com.waylau.rednote.common.dto.UserEditByAdminDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * UserServiceClientFallback UserServiceClient Fallback
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/12/16
 **/
@Component
public class UserServiceClientFallback implements UserServiceClient {
    @Override
    public ResponseEntity<UserDto> findByUserId(Long userId) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(null);
    }

    @Override
    public ResponseEntity<UserDto> getCurrentUser() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(null);
    }

    @Override
    public ResponseEntity<UserDto> findByUsername(String username) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(null);
    }

    @Override
    public ResponseEntity<AllUsersDto> getAllUsers(int page) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(null);
    }

    @Override
    public ResponseEntity<String> updateUserByAdmin(UserEditByAdminDto userEditByAdminDto) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(null);
    }

    @Override
    public ResponseEntity<DeleteResponseDto> deleteUser(Long userId) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(null);
    }

    @Override
    public ResponseEntity<Long> countUsers() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(null);
    }
}
