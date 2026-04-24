package com.waylau.rednote.common.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * NoteExploreDto 笔记探索DTO
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/20
 **/
@Getter
@Setter
public class NoteExploreDto {
    private Long noteId;
    private String title;
    /**
     * 封面
     */
    private String cover;
    /**
     * 作者用户名
     */
    private String username;
    /**
     * 作者头像
     */
    private String avatar;

    /**
     * 作者用户ID
     */
    private Long userId;

    /**
     * 是否点赞
     */
    private boolean isLiked;

    /**
     * 点赞数量
     */
    private long likeCount;

}
