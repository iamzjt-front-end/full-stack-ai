package com.waylau.rednote.filemicroservice.application.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * FileStorageService 文件存储服务
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/18
 **/
public interface FileStorageService {

    /**
     * 保存文件
     *
     * @param file
     * @param filename
     * @return
     */
    String saveFile(MultipartFile file, String filename);

    /**
     * 删除文件
     *
     * @param filePath
     */
    void deleteFile(String filePath);
}
