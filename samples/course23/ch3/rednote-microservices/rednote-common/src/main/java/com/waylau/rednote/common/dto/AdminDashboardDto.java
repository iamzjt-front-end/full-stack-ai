package com.waylau.rednote.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * AdminDashboardDto
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/12/18
 **/
@Getter
@Setter
@AllArgsConstructor
public class AdminDashboardDto {
    private long userCount;
    private long noteCount;
    private long commentCount;
    private List<NoteBrowseCountDto> noteBrowseCountDtoList;
    private List<NoteBrowseTimeDto> noteBrowseTimeDtoList;
}
