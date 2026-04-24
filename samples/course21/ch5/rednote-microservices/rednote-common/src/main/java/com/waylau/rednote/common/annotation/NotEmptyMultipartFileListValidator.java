package com.waylau.rednote.common.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * NotEmptyMultipartFileListValidator 自定义验证器来检查 List<MultipartFile> 中的每个文件是否为空
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/18
 **/
public class NotEmptyMultipartFileListValidator implements ConstraintValidator<NotEmptyMultipartFileList, List<MultipartFile>> {
    @Override
    public boolean isValid(List<MultipartFile> multipartFiles, ConstraintValidatorContext constraintValidatorContext) {
        if (multipartFiles != null && !multipartFiles.isEmpty()) {
            for (MultipartFile multipartFile : multipartFiles) {
                if (multipartFile.isEmpty()) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }
}
