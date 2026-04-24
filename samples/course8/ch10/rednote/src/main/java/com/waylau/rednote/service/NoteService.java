package com.waylau.rednote.service;

import com.waylau.rednote.dto.NoteEditDto;
import com.waylau.rednote.dto.NotePublishDto;
import com.waylau.rednote.entity.Note;
import com.waylau.rednote.entity.User;
import org.springframework.data.domain.Page;

import java.util.Optional;

/**
 * NoteService 笔记服务
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/18
 **/
public interface NoteService {
    /**
     * 创建笔记
     *
     * @param notePublishDto
     * @param author
     * @return
     */
    Note createNote(NotePublishDto notePublishDto, User author);

    /**
     * 分页查询笔记
     *
     * @param userId
     * @param page
     * @param size
     * @return
     */
    Page<Note> getNotesByUser(Long userId, int page, int size);

    /**
     * 根据ID获取笔记
     *
     * @param noteId
     * @return
     */
    Optional<Note> findNoteById(Long noteId);

    /**
     * 更新笔记
     *
     * @param note
     * @param noteEditDto
     */
    void updateNote(Note note, NoteEditDto noteEditDto);

    /**
     * 删除笔记
     *
     * @param note
     */
    void deleteNote(Note note);

    /**
     * 验证用户是否为笔记作者
     *
     * @param noteId
     * @param username
     * @return
     */
    boolean isAuthor(Long noteId, String username);
}
