package com.waylau.rednote.service.impl;

import com.waylau.rednote.entity.Like;
import com.waylau.rednote.entity.Note;
import com.waylau.rednote.entity.User;
import com.waylau.rednote.exception.NoteNotFoundException;
import com.waylau.rednote.repository.LikeRepository;
import com.waylau.rednote.repository.NoteRepository;
import com.waylau.rednote.service.LikeService;
import com.waylau.rednote.service.NoteService;
import com.waylau.rednote.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * LikeServiceImpl 点赞服务
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/21
 **/
@Service
public class LikeServiceImpl implements LikeService {

    @Autowired
    private NoteService noteService;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    @Transactional
    public boolean toggleLike(Long noteId, User user) {
        // 判断笔记是否存在
        Optional<Note> optionalNote = noteService.findNoteById(noteId);
        if (!optionalNote.isPresent()) {
            throw new NoteNotFoundException("");
        }

        // 构建Redis的Key
        String noteLikesKey = "rednote:note:" + noteId + ":likes";
        String likeCountKey = "rednote:note:" + noteId + ":like_count";

        // 从Redis中获取用户点赞记录
        Long userId = user.getUserId();
        boolean hasLikedFromRedis = redisTemplate.opsForSet().isMember(noteLikesKey, userId.toString());

        // 判定Redis是否命中
        boolean hasLiked = false;
        if (hasLikedFromRedis) {
            // 命中，直接返回
            hasLiked = hasLikedFromRedis;
        } else {
            // 未命中，从数据库中查询是否已经点赞
            hasLiked = likeRepository.findByUserUserIdAndNoteNoteId(userId, noteId).isPresent();
        }

        if (hasLiked) {
            // 已点赞，取消点赞
            // 删除Redis中的记录，使用事务保证原子性
            redisTemplate.execute(new SessionCallback<Object>() {
                @Override
                public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                    operations.multi();
                    operations.opsForSet().remove((K) noteLikesKey, (V) userId.toString());
                    operations.opsForValue().decrement((K) likeCountKey);
                    return operations.exec();
                }
            });

            // 删除数据库中的记录
            Optional<Like> optionalLike = likeRepository.findByUserUserIdAndNoteNoteId(userId, noteId);
            likeRepository.delete(optionalLike.get());
            return false;
        } else {
            // 未点赞，添加点赞
            // 增加Redis中的记录，使用事务保证原子性
            redisTemplate.execute(new SessionCallback<Object>() {
                @Override
                public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                    operations.multi();
                    operations.opsForSet().add((K) noteLikesKey, (V) userId.toString());
                    operations.opsForValue().increment((K) likeCountKey);
                    return operations.exec();
                }
            });

            // 添加数据库中的记录
            Like like = new Like();
            like.setUser(user);
            like.setNote(optionalNote.get());

            likeRepository.save(like);
            return true;
        }

        // 查询用户是否已点赞
        /*Optional<Like> optionalLike = likeRepository.findByUserUserIdAndNoteNoteId(user.getUserId(), noteId);
        if (optionalLike.isPresent()) {
            // 已点赞，取消点赞
            likeRepository.delete(optionalLike.get());
            return false;
        } else {
            // 未点赞，添加点赞
            Like like = new Like();
            like.setUser(user);
            like.setNote(optionalNote.get());

            likeRepository.save(like);
            return true;
        }*/
    }

    @Override
    public long getLikeCount(Long noteId) {
        // 从Redis中获取点赞数
        String likeCountKey = "rednote:note:" + noteId + ":like_count";
        String likeCountFromRedis = redisTemplate.opsForValue().get(likeCountKey);

        // 判定Redis是否命中
        if (likeCountFromRedis != null) {
            // 命中，直接返回
            return Long.parseLong(likeCountFromRedis);
        } else {
            // 未命中，从数据库中查询，再存储到Redis
            long likeCountFromDb = likeRepository.countByNoteNoteId(noteId);
            redisTemplate.opsForSet().add(likeCountKey, String.valueOf(noteId));

            return likeCountFromDb;
        }

        /*return likeRepository.countByNoteNoteId(noteId);*/
    }
}
