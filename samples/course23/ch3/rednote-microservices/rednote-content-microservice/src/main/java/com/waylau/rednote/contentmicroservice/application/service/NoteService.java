package com.waylau.rednote.contentmicroservice.application.service;

import com.waylau.rednote.common.dto.NoteExploreDto;
import com.waylau.rednote.common.dto.UserDto;
import com.waylau.rednote.common.dto.NoteBrowseCountDto;
import com.waylau.rednote.common.dto.NoteBrowseTimeDto;
import com.waylau.rednote.contentmicroservice.application.dto.NoteEditDto;
import com.waylau.rednote.contentmicroservice.application.dto.NotePublishDto;
import com.waylau.rednote.contentmicroservice.domain.model.entity.Note;
import org.springframework.data.domain.Page;

import java.util.List;
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
    Note createNote(NotePublishDto notePublishDto, UserDto author);

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

    /**
     * 分页查询笔记
     *
     * @param page
     * @param pageSize
     * @param category
     * @return
     */
    Page<Note> getNotesByPage(int page, int pageSize, String category);

    /**
     * 搜索分页查询笔记
     *
     * @param page
     * @param pageSize
     * @param category
     * @param query
     * @return
     */
    Page<Note> getNotesByPageAndQuery(int page, int pageSize, String category, String query);

    /**
     * 获取笔记总数
     */
    long countNotes();

    /**
     * 递增访问量
     *
     * @param noteId
     */
    void incrementBrowseCount(Long noteId);

    /**
     * 递增访问时长
     *
     * @param noteId
     * @param browseTime
     */
    void incrementBrowseTime(Long noteId, long browseTime);

    /**
     * 根据访问量获取笔记列表
     *
     * @param page
     * @param size
     */
    List<NoteBrowseCountDto> getNotesByBrowseCount(int page, int size);

    /**
     * 根据访问时长获取笔记列表
     *
     * @param page
     * @param size
     */
    List<NoteBrowseTimeDto> getNotesByBrowseTime(int page, int size);

    /**
     * 笔记转为笔记探索DTO
     *
     * @param note
     * @param user
     * @return
     */
    NoteExploreDto toExploreDto(Note note, UserDto user);
}
