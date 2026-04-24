package com.waylau.rednote.contentmicroservice.application.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * NoteBrowseTimeDto
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/09/05
 **/
@Getter
@Setter
public class NoteBrowseTimeDto {
    private Long noteId;
    private String title;
    private long browseTime;
}
