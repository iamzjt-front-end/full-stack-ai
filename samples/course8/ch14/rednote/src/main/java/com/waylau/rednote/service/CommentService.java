package com.waylau.rednote.service;

import com.waylau.rednote.entity.Comment;
import com.waylau.rednote.entity.Note;
import com.waylau.rednote.entity.User;

import java.util.List;
import java.util.Optional;

/**
 * CommentService 评论服务
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/22
 **/
public interface CommentService {
    /**
     * 创建评论
     *
     * @param note
     * @param user
     * @param content
     * @return
     */
    Comment createComment(Note note, User user, String content);

    /**
     * 删除评论
     *
     * @param comment
     */
    void deleteComment(Comment comment);

    /**
     * 根据评论ID获取评论
     *
     * @param commentId
     * @return
     */
    Optional<Comment> findCommentById(Long commentId);

    /**
     * 根据笔记ID获取笔记的根评论
     *
     * @return
     */
    List<Comment> getCommentsByNoteId(Long noteId);

    /**
     * 回复评论
     *
     * @param note
     * @param parentComment
     * @param user
     * @param content
     * @return
     */
    Comment replyToComment(Note note, Comment parentComment, User user, String content);
}
