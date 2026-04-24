package com.waylau.rednote.contentmicroservice.application.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * LikeEvent 点赞事件
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/09/05
 **/
@Getter
@Setter
public class LikeEvent extends UserActionEvent {
    /**
     * 笔记ID
     */
    private Long noteId;

    /**
     * 点赞/取消点赞
     */
    private boolean isLike;
}
