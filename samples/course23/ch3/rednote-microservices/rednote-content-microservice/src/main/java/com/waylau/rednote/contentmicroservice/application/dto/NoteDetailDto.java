package com.waylau.rednote.contentmicroservice.application.dto;

import com.waylau.rednote.common.dto.UserDto;
import com.waylau.rednote.contentmicroservice.domain.model.entity.Note;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * NoteDetailDto 笔记详情页展示DTO
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/09/09
 **/
@Getter
@Setter
public class NoteDetailDto {
    // 以下字段来自Note
    private Long noteId;
    private String title;
    private String content;
    private List<String> images = new ArrayList<>();
    private List<String> topics = new ArrayList<>();
    private String category;
    // 以下字段来自User
    private String username;
    private String avatar;
    private Long userId;
    private boolean isLiked;
    private long likeCount;

    /*public static NoteDetailDto toNoteDetailDto(Note note, User currentUser) {
        NoteDetailDto noteDetailDto = new NoteDetailDto();
        noteDetailDto.setNoteId(note.getNoteId());
        noteDetailDto.setTitle(note.getTitle());
        noteDetailDto.setContent(note.getContent());
        noteDetailDto.setImages(note.getImages());
        noteDetailDto.setTopics(note.getTopics());
        noteDetailDto.setCategory(note.getCategory());
        noteDetailDto.setLikeCount(note.getLikeCount());

        User author = note.getAuthor();
        noteDetailDto.setUsername(author.getUsername());
        noteDetailDto.setAvatar(author.getAvatar());
        noteDetailDto.setUserId(author.getUserId());

        noteDetailDto.setLiked(note.isLikedByUser(currentUser.getUserId()));

        return noteDetailDto;
    }*/
    public static NoteDetailDto toNoteDetailDto(Note note, UserDto currentUser) {
        NoteDetailDto noteDetailDto = new NoteDetailDto();
        noteDetailDto.setNoteId(note.getNoteId());
        noteDetailDto.setTitle(note.getTitle());
        noteDetailDto.setContent(note.getContent());
        noteDetailDto.setImages(note.getImages());
        noteDetailDto.setTopics(note.getTopics());
        noteDetailDto.setCategory(note.getCategory());
        noteDetailDto.setLikeCount(note.getLikeCount());

        noteDetailDto.setUsername(currentUser.getUsername());
        noteDetailDto.setAvatar(currentUser.getAvatar());
        noteDetailDto.setUserId(currentUser.getUserId());

        noteDetailDto.setLiked(note.isLikedByUser(currentUser.getUserId()));

        return noteDetailDto;
    }

}
