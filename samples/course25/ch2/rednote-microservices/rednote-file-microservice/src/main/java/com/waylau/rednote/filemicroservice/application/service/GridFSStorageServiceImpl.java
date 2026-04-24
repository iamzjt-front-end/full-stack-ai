package com.waylau.rednote.filemicroservice.application.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.waylau.rednote.common.exception.FileStorageException;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * GridFSStorageServiceImpl GridFS存储服务
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/09/06
 **/
@Service
public class GridFSStorageServiceImpl implements GridFSStorageService {
    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private GridFSBucket gridFSBucket;

    @Override
    public String uploadImage(MultipartFile multipartFile) {
        // 创建文件元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("contentType", multipartFile.getContentType());
        metadata.put("size", multipartFile.getSize());
        metadata.put("uploadDate", LocalDateTime.now());

        // 存储文件到GridFS
        try {
            ObjectId objectId = gridFsTemplate.store(
                    multipartFile.getInputStream(),
                    UUID.randomUUID().toString(),
                    multipartFile.getContentType(),
                    metadata);

            return objectId.toString();
        } catch (IOException e) {
            throw new FileStorageException("文件上传失败：" + multipartFile.getOriginalFilename(), e);
        }
    }

    @Override
    public GridFSFile downloadImage(String fileId) {
        return gridFsTemplate.findOne(new Query(Criteria.where("_id").is(fileId)));
    }

    @Override
    public void deleteImage(String fileId) {
        gridFsTemplate.delete(new Query(Criteria.where("_id").is(fileId)));
    }

    @Override
    public InputStream getImageStream(String fileId) {
        GridFSFile gridFSFile = downloadImage(fileId);
        if (gridFSFile != null) {
            return gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        }

        return null;
    }
}
