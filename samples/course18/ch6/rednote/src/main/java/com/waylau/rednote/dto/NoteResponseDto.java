package com.waylau.rednote.dto;

import com.waylau.rednote.entity.Note;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * NoteResponseDto 探索笔记的响应对象
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/20
 **/
@Getter
@Setter
public class NoteResponseDto {
    /**
     * 笔记列表
     */
    /*private List<Note> notes;*/
    private List<NoteExploreDto> notes;

    /**
     * 是否还有更多
     */
    private boolean hasMore;
}
