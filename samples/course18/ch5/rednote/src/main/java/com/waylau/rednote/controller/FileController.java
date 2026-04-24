package com.waylau.rednote.controller;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.waylau.rednote.service.GridFSStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
