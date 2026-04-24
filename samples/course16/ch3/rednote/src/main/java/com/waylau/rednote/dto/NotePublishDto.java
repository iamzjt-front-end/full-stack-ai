package com.waylau.rednote.dto;

import com.waylau.rednote.annotation.NotEmptyMultipartFileList;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * NotePublishDto 笔记发布DTO
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/18
 **/
@Getter
@Setter
public class NotePublishDto {
    @NotEmpty(message = "标题不能为空")
    @Size(max = 60, message = "标题长度不能超过60个字符")
    private String title;

    @NotEmpty(message = "内容不能为空")
    @Size(max = 900, message = "内容长度不能超过900个字符")
    private String content;

    private String topics;

    @NotEmpty(message = "分类不能为空")
    private String category;

    /*@NotEmpty(message = "图片不能为空")*/
    // 添加自定义的验证器注解
    @NotEmptyMultipartFileList(message = "图片不能为空")
    @Size(min = 1, max = 9, message = "上传图片最多9张")
    private List<MultipartFile> images;
}
