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

    /**
     * 删除笔记
     *
     * @param note
     */
    void delete(Note note);

    /**
     * 根据分类、分页查询笔记
     *
     * @param category
     * @param pageable
     * @return
     */
    Page<Note> findByCategory(String category, Pageable pageable);

    /**
     * 分页查询笔记
     */
    Page<Note> findAll(Pageable pageable);

    /**
     * 根据分类和话题标签分页查询笔记
     *
     * @param category
     * @param query
     * @param pageable
     * @return
     */
    Page<Note> findByCategoryAndTopicsContaining(String category, String query, Pageable pageable);

    /**
     * 根据话题标签分页查询笔记
     *
     * @param query
     * @param pageable
     * @return
     */
    Page<Note> findByTopicsContaining(String query, Pageable pageable);
}
