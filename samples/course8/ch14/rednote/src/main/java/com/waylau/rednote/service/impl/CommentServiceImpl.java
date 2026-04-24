package com.waylau.rednote.service.impl;

import com.waylau.rednote.entity.Comment;
import com.waylau.rednote.entity.Note;
import com.waylau.rednote.entity.User;
import com.waylau.rednote.repository.CommentRepository;
import com.waylau.rednote.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * CommentServiceImpl 评论服务
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/22
 **/
@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    private CommentRepository commentRepository;

    @Override
    public Comment createComment(Note note, User user, String content) {
        Comment comment = new Comment();
        comment.setNote(note);
        comment.setUser(user);
        comment.setContent(content);

        return commentRepository.save(comment);
    }

    @Override
    public void deleteComment(Comment comment) {
        commentRepository.delete(comment);
    }

    @Override
    public Optional<Comment> findCommentById(Long commentId) {
        return commentRepository.findByCommentId(commentId);
    }

    @Override
    public List<Comment> getCommentsByNoteId(Long noteId) {
        /*return commentRepository.findByParentIsNullAndNoteNoteIdOrderByCreateAtDesc(noteId);*/
        List<Comment> rootComments = commentRepository.findByParentIsNullAndNoteNoteIdOrderByCreateAtDesc(noteId);

        // 递归加载所有的回复及其子回复到根评论上
        rootComments.forEach(rootComment -> {
            // 构建一个能包含回复及其子回复的列表，从1到N层的所有回复
            List<Comment> allReplies = new ArrayList<>();

            // 第一层的回复直接放到allReplies列表中
            List<Comment> firstLevelReplies = rootComment.getReplies();
            allReplies.addAll(firstLevelReplies);

            // 第二层及其后续的回复，就递归而后添加到allReplies列表中
            loadRepliesRecursively(firstLevelReplies, allReplies);

            // allReplies列表按照创建时间倒序排序
            rootComment.setReplies(allReplies.stream().sorted(Comparator.comparing(Comment::getCreateAt).reversed()).toList());
        });

        return rootComments;
    }

    // 递归加载回复
    private void loadRepliesRecursively(List<Comment> replies, List<Comment> allReplies) {
        replies.forEach(reply -> {
            List<Comment> sonReplies = reply.getReplies();
            allReplies.addAll(sonReplies);

            loadRepliesRecursively(sonReplies, allReplies);
        });
    }

    @Override
    public Comment replyToComment(Note note, Comment parentComment, User user, String content) {
        Comment reply = new Comment();
        reply.setNote(note);
        reply.setUser(user);
        reply.setContent(content);
        reply.setParent(parentComment);

        parentComment.getReplies().add(reply);

        return commentRepository.save(reply);
    }
}
