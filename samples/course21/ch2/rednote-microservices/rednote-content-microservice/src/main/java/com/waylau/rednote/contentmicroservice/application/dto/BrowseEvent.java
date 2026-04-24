package com.waylau.rednote.contentmicroservice.application.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * BrowseEvent 浏览事件
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/09/05
 **/
@Getter
@Setter
public class BrowseEvent extends UserActionEvent {
    /**
     * 笔记ID
     */
    private Long noteId;

    /**
     * 浏览时长（秒）
     */
    private long browseTime;
}
