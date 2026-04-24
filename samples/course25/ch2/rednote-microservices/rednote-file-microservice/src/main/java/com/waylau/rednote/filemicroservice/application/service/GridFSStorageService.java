package com.waylau.rednote.filemicroservice.application.service;

import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * GridFSStorageService GridFS存储服务
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/09/06
 **/
public interface GridFSStorageService {
    /**
     * 上传图片
     *
     * @param multipartFile
     * @return
     */
    String uploadImage(MultipartFile multipartFile);

    /**
     * 下载图片
     *
     * @param fileId
     * @return
     */
    GridFSFile downloadImage(String fileId);

    /**
     * 删除图片
     *
     * @param fileId
     */
    void deleteImage(String fileId);

    /**
     * 获取图片流
     *
     * @param fileId
     * @return
     */
    InputStream getImageStream(String fileId);
}
