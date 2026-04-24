package com.waylau.rednote.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * AllUsersDto
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/12/18
 **/
@Getter
@Setter
@AllArgsConstructor
public class AllUsersDto {
    private List<UserEditByAdminDto> userList;

    private int currentPage;

    private int totalPages;
}
