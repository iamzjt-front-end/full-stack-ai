package com.waylau.rednote.common.interfaces.client;

import com.waylau.rednote.common.dto.AdminDashboardDto;
import com.waylau.rednote.common.dto.NotesWithUserDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * ContentServiceClientFallback ContentServiceClient Fallback
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/12/16
 **/
// 降级实现类
@Component
public class ContentServiceClientFallback implements ContentServiceClient {
    @Override
    public ResponseEntity<NotesWithUserDto> getNotesByUser(Long userId, int page, int size) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(null);
    }

    @Override
    public ResponseEntity<AdminDashboardDto> dashboard() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(null);
    }
}
