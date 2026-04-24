package com.waylau.rednote.repository;

import com.waylau.rednote.entity.Comment;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

/**
 * CommentRepository 评论资源库
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/22
 **/
public interface CommentRepository extends Repository<Comment, Long> {
    Comment save(Comment comment);

    Optional<Comment> findByCommentId(Long commentId);

    void delete(Comment comment);

    /**
     * 查找根评论
     *
     * @param noteId
     * @return
     */
    List<Comment> findByParentIsNullAndNoteNoteIdOrderByCreateAtDesc(Long noteId);

    /**
     * 根据父评论ID获取它的子评论
     *
     * @param parentCommentId
     * @return
     */
    List<Comment> findByParentCommentId(Long parentCommentId);

    /**
     * 统计评论总数
     *
     * @return
     */
    long count();

}
