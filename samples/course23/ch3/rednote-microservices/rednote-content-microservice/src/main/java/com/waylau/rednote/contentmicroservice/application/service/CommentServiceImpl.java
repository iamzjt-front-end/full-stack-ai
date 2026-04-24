package com.waylau.rednote.contentmicroservice.application.service;

import com.waylau.rednote.common.dto.UserDto;
import com.waylau.rednote.common.interfaces.client.UserServiceClient;
import com.waylau.rednote.contentmicroservice.application.dto.CommentResponseDto;
import com.waylau.rednote.contentmicroservice.domain.model.entity.Comment;
import com.waylau.rednote.contentmicroservice.domain.model.entity.Note;
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

    @Autowired
    private UserServiceClient userServiceClient;

    @Override
    @Transactional
    public Comment createComment(Note note, UserDto user, String content) {
        // 在Redis递增评论数
        redisTemplate.opsForValue().increment(COMMENT_COUNT_KEY);

        Comment comment = new Comment();
        comment.setNote(note);

        // 将用户对象改为用户ID
        /*comment.setUser(user);*/
        comment.setUserId(user.getUserId());
        comment.setContent(content);

        // 冗余username
        comment.setUsername(user.getUsername());

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
    public Comment replyToComment(Note note, Comment parentComment, UserDto user, String content) {
        // 在Redis递增评论数
        redisTemplate.opsForValue().increment(COMMENT_COUNT_KEY);

        Comment reply = new Comment();
        reply.setNote(note);

        // 将用户对象改为用户ID
        /*reply.setUser(user);*/
        reply.setUserId(user.getUserId());
        reply.setContent(content);
        reply.setParent(parentComment);

        // 冗余username
        reply.setUsername(user.getUsername());

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

    @Override
    public CommentResponseDto toCommentResponseDto(Comment comment) {
        if (comment == null) {
            return null;
        }

        CommentResponseDto commentResponseDto = new CommentResponseDto();
        commentResponseDto.setCommentId(comment.getCommentId());
        commentResponseDto.setContent(comment.getContent());
        commentResponseDto.setCreateAt(comment.getCreateAt());

        /*User user = comment.getUser();*/
        UserDto user = userServiceClient.findByUserId(comment.getUserId()).getBody();

        commentResponseDto.setUserId(user.getUserId());
        commentResponseDto.setUsername(user.getUsername());
        commentResponseDto.setAvatar(user.getAvatar());

        Note note = comment.getNote();
        commentResponseDto.setNoteId(note.getNoteId());

        Comment parentComment = comment.getParent();
        if (parentComment != null) {
            commentResponseDto.setParentCommentId(parentComment.getCommentId());
            commentResponseDto.setParentCommentUsername(parentComment.getUsername());
        }

        List<Comment> replies = comment.getReplies();
        for (Comment reply : replies) {
            CommentResponseDto replyDto = toCommentResponseDto(reply);
            commentResponseDto.getReplies().add(replyDto);
        }

        return commentResponseDto;
    }
}
