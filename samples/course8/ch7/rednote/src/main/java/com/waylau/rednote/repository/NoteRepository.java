package com.waylau.rednote.repository;

import com.waylau.rednote.entity.Note;
import org.springframework.data.repository.Repository;

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
}
