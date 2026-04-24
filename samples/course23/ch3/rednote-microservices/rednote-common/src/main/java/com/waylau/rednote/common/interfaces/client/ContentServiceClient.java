package com.waylau.rednote.common.interfaces.client;

import com.waylau.rednote.common.dto.AdminDashboardDto;
import com.waylau.rednote.common.dto.NotesWithUserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * ContentServiceClient ContentService Feign Client
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/12/16
 **/
@FeignClient(name = "rednote-content-microservice",
        // 指定了降级实现类
        fallback = ContentServiceClientFallback.class)
public interface ContentServiceClient {
    @GetMapping("/note/user/{userId}")
    ResponseEntity<NotesWithUserDto> getNotesByUser(@PathVariable Long userId,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "12") int size);

    @GetMapping("/log/dashboard")
    ResponseEntity<AdminDashboardDto> dashboard();
}
