package com.waylau.rednote.contentmicroservice.application.service;

import com.waylau.rednote.contentmicroservice.domain.model.entity.Comment;
import com.waylau.rednote.contentmicroservice.domain.model.entity.Note;
import com.waylau.rednote.common.entity.User;
import com.waylau.rednote.contentmicroservice.domain.repository.CommentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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
    private static final String COMMENT_COUNT_KEY = "rednote:comment:comment_count";

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    @Transactional
    public Comment createComment(Note note, User user, String content) {
        // 在Redis递增评论数
        redisTemplate.opsForValue().increment(COMMENT_COUNT_KEY);

        Comment comment = new Comment();
        comment.setNote(note);
        comment.setUser(user);
        comment.setContent(content);

        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Comment comment) {
        // 在Redis递减评论数
        redisTemplate.opsForValue().decrement(COMMENT_COUNT_KEY);

        commentRepository.delete(comment);
    }

    @Override
    public Optional<Comment> findCommentById(Long commentId) {
        return commentRepository.findByCommentId(commentId);
    }

    @Override
    public List<Comment> getCommentsByNoteId(Long noteId) {
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
    @Transactional
    public Comment replyToComment(Note note, Comment parentComment, User user, String content) {
        // 在Redis递增评论数
        redisTemplate.opsForValue().increment(COMMENT_COUNT_KEY);

        Comment reply = new Comment();
        reply.setNote(note);
        reply.setUser(user);
        reply.setContent(content);
        reply.setParent(parentComment);

        parentComment.getReplies().add(reply);

        return commentRepository.save(reply);
    }

    @Override
    public long countComments() {
        // 从Redis中获取评论数
        String countFromRedis = redisTemplate.opsForValue().get(COMMENT_COUNT_KEY);
        // 判断Redis是否命中
        if (countFromRedis != null) {
            // 命中，直接返回
            return Long.parseLong(countFromRedis);
        } else {
            // 未命中，从数据库中查询，再存储到Redis
            long countFromDb = commentRepository.count();
            redisTemplate.opsForValue().set(COMMENT_COUNT_KEY, String.valueOf(countFromDb));

            return countFromDb;
        }
    }
}
