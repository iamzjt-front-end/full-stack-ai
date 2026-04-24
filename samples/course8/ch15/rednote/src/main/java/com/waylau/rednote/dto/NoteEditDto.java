package com.waylau.rednote.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * NoteEditDto 笔记编辑DTO
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/19
 **/
@Getter
@Setter
public class NoteEditDto {
    @NotNull
    private Long noteId;

    @NotEmpty(message = "标题不能为空")
    @Size(max = 60, message = "标题长度不能超过60个字符")
    private String title;

    @NotEmpty(message = "内容不能为空")
    @Size(max = 900, message = "内容长度不能超过900个字符")
    private String content;

    private String topics;

    @NotEmpty(message = "分类不能为空")
    private String category;

    private List<String> images = new ArrayList<>();
}
