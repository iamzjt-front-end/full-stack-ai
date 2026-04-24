package com.waylau.rednote.filemicroservice.interfaces.controller;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.waylau.rednote.filemicroservice.application.service.GridFSStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * FileController 文件控制器
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/09/06
 **/
@RestController
@RequestMapping("/file")
public class FileController {
    @Autowired
    private GridFSStorageService gridFSStorageService;

    /**
     * 下载文件
     *
     * @param fileId
     * @return
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<Resource> downloadImage(@PathVariable String fileId) {
        GridFSFile file = gridFSStorageService.downloadImage(fileId);

        if (file == null) {
            return ResponseEntity.notFound().build();
        }

        // 从元数据获取内容类型
        String contentType = file.getMetadata().getString("contentType");

        InputStreamResource resource = new InputStreamResource(gridFSStorageService.getImageStream(fileId));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(file.getLength())
                .body(resource);
    }

    /**
     * 上传文件
     *
     * @param file
     * @return
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadImage(@RequestPart("file") MultipartFile file) {
        String fileId = null;

        // 验证文件
        if (file != null && !file.isEmpty()) {
            // 处理文件上传
            fileId = gridFSStorageService.uploadImage(file);
        }

        return ResponseEntity.ok(fileId);
    }

    /**
     * 删除文件
     *
     * @param fileId
     * @return
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<String> deleteImage(@PathVariable String fileId) {
        // 验证文件是否存在
        GridFSFile file = gridFSStorageService.downloadImage(fileId);

        if (file == null) {
            return ResponseEntity.ok("文件不存在");
        }

        gridFSStorageService.deleteImage(fileId);

        return ResponseEntity.ok("文件删除成功");
    }
}
