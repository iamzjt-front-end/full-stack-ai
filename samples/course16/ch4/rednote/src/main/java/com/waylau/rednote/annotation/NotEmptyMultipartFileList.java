package com.waylau.rednote.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * NotEmptyMultipartFileList 验证 List<MultipartFile> 中的每个 MultipartFile 是否为空
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/18
 **/
@Documented
@Constraint(validatedBy = NotEmptyMultipartFileListValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotEmptyMultipartFileList {
    String message() default "文件列表不能包含空文件";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
