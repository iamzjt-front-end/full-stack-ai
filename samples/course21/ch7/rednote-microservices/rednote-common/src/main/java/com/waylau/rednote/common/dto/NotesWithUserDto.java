package com.waylau.rednote.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * NotesWithUserDto NotesWithUser Dto
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/12/16
**/
@Getter
@Setter
@AllArgsConstructor
public class NotesWithUserDto {
    private UserDto user;
    List<NoteExploreDto> noteList;
    private int currentPage;
    private int totalPages;
}
