package com.waylau.rednote.dto;

import com.waylau.rednote.entity.Note;
import lombok.Getter;
import lombok.Setter;

/**
 * NoteExploreDto 笔记探索DTO
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/20
 **/
@Getter
@Setter
public class NoteExploreDto {
    private Long noteId;
    private String title;
    /**
     * 封面
     */
    private String cover;
    /**
     * 作者用户名
     */
    private String username;
    /**
     * 作者头像
     */
    private String avatar;

    /**
     * 作者用户ID
     */
    private Long userId;

    public static NoteExploreDto toExploreDto(Note note) {
        NoteExploreDto noteExploreDto = new NoteExploreDto();
        noteExploreDto.setNoteId(note.getNoteId());
        noteExploreDto.setTitle(note.getTitle());
        noteExploreDto.setCover(note.getImages().get(0));
        noteExploreDto.setUsername(note.getAuthor().getUsername());
        noteExploreDto.setAvatar(note.getAuthor().getAvatar());
        noteExploreDto.setUserId(note.getAuthor().getUserId());

        return noteExploreDto;
    }
}
