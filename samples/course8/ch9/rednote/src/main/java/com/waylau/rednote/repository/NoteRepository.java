package com.waylau.rednote.repository;

import com.waylau.rednote.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import java.util.Optional;

/**
 * NoteRepository 笔记资源库
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/18
 **/
public interface NoteRepository extends Repository<Note, Long> {
    /**
     * 保存笔记
     *
     * @param note
     * @return
     */
    Note save(Note note);

    /**
     * 根据作者的用户ID分页查找笔记列表
     *
     * @param userId
     * @param pageable
     * @return
     */
    Page<Note> findByAuthorUserId(Long userId, Pageable pageable);

    /**
     * 根据笔记ID查找笔记
     *
     * @param noteId
     * @return
     */
    Optional<Note> findByNoteId(Long noteId);
}
