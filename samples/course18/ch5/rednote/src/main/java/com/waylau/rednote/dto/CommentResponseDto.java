package com.waylau.rednote.dto;

import com.waylau.rednote.entity.Comment;
import com.waylau.rednote.entity.Note;
import com.waylau.rednote.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * CommentResponseDto 评论的响应对象
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/22
 **/
@Getter
@Setter
public class CommentResponseDto {

    // 以下是从Comment对象中获取
    private Long commentId;
    private String content;
    private LocalDateTime createAt;

    // 以下是从User对象中获取
    private Long userId;
    private String username;
    private String avatar;

    // 以下是从Note对象中获取
    private Long noteId;

    // 以下是从Comment的parent对象中获取
    private Long parentCommentId;

    // 以下是从Comment的parent对象中的User对象中获取
    private String parentCommentUsername;

    // 子评论
    private List<CommentResponseDto> replies = new ArrayList<>();

    public static CommentResponseDto toCommentResponseDto(Comment comment) {
        if (comment == null) {
            return null;
        }

        CommentResponseDto commentResponseDto = new CommentResponseDto();
        commentResponseDto.setCommentId(comment.getCommentId());
        commentResponseDto.setContent(comment.getContent());
        commentResponseDto.setCreateAt(comment.getCreateAt());

        User user = comment.getUser();
        commentResponseDto.setUserId(user.getUserId());
        commentResponseDto.setUsername(user.getUsername());
        commentResponseDto.setAvatar(user.getAvatar());

        Note note = comment.getNote();
        commentResponseDto.setNoteId(note.getNoteId());

        Comment parentComment = comment.getParent();
        if (parentComment != null) {
            commentResponseDto.setParentCommentId(parentComment.getCommentId());

            User parentCommentUser = parentComment.getUser();
            commentResponseDto.setParentCommentUsername(parentCommentUser.getUsername());
        }

        List<Comment> replies = comment.getReplies();
        for (Comment reply : replies) {
            CommentResponseDto replyDto = toCommentResponseDto(reply);
            commentResponseDto.getReplies().add(replyDto);
        }

        return commentResponseDto;
    }
}
