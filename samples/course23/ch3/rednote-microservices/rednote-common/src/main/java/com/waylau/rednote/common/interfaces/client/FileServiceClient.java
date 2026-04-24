package com.waylau.rednote.common.interfaces.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * FileServiceClient FileService Feign Client
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/12/16
 **/
@FeignClient(name = "rednote-file-microservice",
        // 指定了降级实现类
        fallback = FileServiceClientFallback.class)
public interface FileServiceClient {
    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<String> uploadImage(@RequestPart("file") MultipartFile file);

    @DeleteMapping("/file/{fileId}")
    ResponseEntity<String> deleteImage(@PathVariable String fileId);
}
