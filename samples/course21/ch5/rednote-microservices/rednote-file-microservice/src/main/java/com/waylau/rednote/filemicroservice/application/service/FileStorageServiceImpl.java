package com.waylau.rednote.filemicroservice.application.service;

import com.waylau.rednote.common.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;

/**
 * FileStorageServiceImpl 文件存储服务
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/18
 **/
@Service
public class FileStorageServiceImpl implements FileStorageService {

    // 文件存储根路径，可以配置在应用配置文件中
    @Value("${file.upload-dir:/data/rednote}")
    private String uploadDir;

    // 静态资源访问路径前缀，可以配置在应用配置文件中
    @Value("${file.static-path-prefix:/uploads/}")
    private String staticPathPrefix;

    @Override
    public String saveFile(MultipartFile file, String filename) {
        // 确保文件名唯一
        String uniqueFileName = UUID.randomUUID() + "_" + filename;

        // 生成文件存储路径，按照日期分目录，提高文件系统的性能
        String subDir = LocalDate.now().toString();
        Path uploadPath = Paths.get(uploadDir + staticPathPrefix + subDir);

        try {
            // 创建目录（如果不存在）
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 拷贝文件。使用完后释放资源
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, uploadPath.resolve(uniqueFileName), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            // 抛出自定义运行时异常
            throw new FileStorageException("文件上传失败：" + filename, e);
        }

        // 返回可访问的URL路径
        return staticPathPrefix + subDir + "/" + uniqueFileName;
    }

    @Override
    public void deleteFile(String filePath) {
        // 判定文件路径是否为空
        if (filePath == null || filePath.isEmpty()) {
            return;
        }

        // 安全检查，确保路径在上传目录内
        Path fullPath = Paths.get(uploadDir + filePath).normalize();

        try {
            // 删除文件
            Files.deleteIfExists(fullPath);
        } catch (Exception e) {
            // 抛出自定义运行时异常
            throw new FileStorageException("文件删除失败：" + filePath, e);
        }
    }
}
