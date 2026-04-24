package com.waylau.rednote.contentmicroservice.interfaces.controller;

import com.waylau.rednote.contentmicroservice.application.dto.LikeResponseDto;
import com.waylau.rednote.common.entity.User;
import com.waylau.rednote.contentmicroservice.application.service.LikeService;
import com.waylau.rednote.usermicroservice.application.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * LikeController 点赞控制器
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/21
 **/
@Controller
@RequestMapping("/like")
public class LikeController {
    @Autowired
    private LikeService likeService;

    @Autowired
    private UserService userService;

    /**
     * 处理点赞、取消点赞请求
     *
     * @param noteId
     * @return
     */
    @PostMapping("/{noteId}")
    public ResponseEntity<LikeResponseDto> toggleLike(@PathVariable Long noteId) {
        User currentUser = userService.getCurrentUser();

        boolean isLiked = likeService.toggleLike(noteId, currentUser);
        long likeCount = likeService.getLikeCount(noteId);

        return ResponseEntity.ok(new LikeResponseDto(isLiked, likeCount));
    }
}
