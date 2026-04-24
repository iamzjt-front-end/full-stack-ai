package com.waylau.rednote.contentmicroservice.application.service;

import com.waylau.rednote.common.entity.User;

/**
 * LikeService 点赞服务
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/21
 **/
public interface LikeService {
    /**
     * 点赞\取消点赞
     *
     * @param noteId
     * @param user
     * @return
     */
    boolean toggleLike(Long noteId, User user);

    /**
     * 获取笔记的点赞数
     *
     * @param noteId
     * @return
     */
    long getLikeCount(Long noteId);
}
