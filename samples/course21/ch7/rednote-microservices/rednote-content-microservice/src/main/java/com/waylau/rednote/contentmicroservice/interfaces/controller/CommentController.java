package com.waylau.rednote.contentmicroservice.interfaces.controller;

import com.waylau.rednote.common.dto.UserDto;
import com.waylau.rednote.common.interfaces.client.UserServiceClient;
import com.waylau.rednote.contentmicroservice.application.dto.CommentResponseDto;
import com.waylau.rednote.contentmicroservice.domain.model.entity.Comment;
import com.waylau.rednote.contentmicroservice.domain.model.entity.Note;
import com.waylau.rednote.common.exception.CommentNotFoundException;
import com.waylau.rednote.common.exception.NoteNotFoundException;
import com.waylau.rednote.contentmicroservice.application.service.CommentService;
import com.waylau.rednote.contentmicroservice.application.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CommentController 评论控制器
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/22
 **/
@Controller
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private NoteService noteService;

    @Autowired
    /*private UserService userService;*/
    private UserServiceClient userServiceClient;

    @Autowired
    private CommentService commentService;

    // 处理创建评论的请求
    @PostMapping("/{noteId}")
    public ResponseEntity<CommentResponseDto> createComment(@PathVariable("noteId") Long noteId,
                                                            @RequestBody String content) {
        // 判定笔记是否存在
        Optional<Note> optionalNote = noteService.findNoteById(noteId);
        if (!optionalNote.isPresent()) {
            throw new NoteNotFoundException("");
        }

        Note note = optionalNote.get();
        /*User user = userService.getCurrentUser();*/
        UserDto user = userServiceClient.getCurrentUser().getBody();

        Comment comment = commentService.createComment(note, user, content);

        // 将Comment对象转换成DTO对象
        CommentResponseDto commentResponseDto = commentService.toCommentResponseDto(comment);

        return ResponseEntity.ok(commentResponseDto);
    }

    // 处理获取笔记评论列表的请求
    @GetMapping("/{noteId}")
    public ResponseEntity<List<CommentResponseDto>> getCommentsByNoteId(@PathVariable("noteId") Long noteId) {
        // 判定笔记是否存在
        Optional<Note> optionalNote = noteService.findNoteById(noteId);
        if (!optionalNote.isPresent()) {
            throw new NoteNotFoundException("");
        }

        List<Comment> comments = commentService.getCommentsByNoteId(noteId);

        // 将Comment对象转换成DTO对象
        List<CommentResponseDto> commentResponseDtoList = comments.stream().map(commentService::toCommentResponseDto)
                .collect(Collectors.toUnmodifiableList());

        return ResponseEntity.ok(commentResponseDtoList);
    }

    // 处理创建回复的请求
    @PostMapping("/{noteId}/reply/{parentCommentId}")
    public ResponseEntity<CommentResponseDto> replyToComment(@PathVariable("noteId") Long noteId,
                                                             @PathVariable("parentCommentId") Long parentCommentId,
                                                             @RequestBody String content) {
        // 判定笔记是否存在
        Optional<Note> optionalNote = noteService.findNoteById(noteId);
        if (!optionalNote.isPresent()) {
            throw new NoteNotFoundException("");
        }

        // 判定父级评论是否存在
        Optional<Comment> optionalParentComment = commentService.findCommentById(parentCommentId);
        if (!optionalParentComment.isPresent()) {
            throw new CommentNotFoundException("");
        }

        // 判定父级评论是否属于该笔记
        Comment parentComment = optionalParentComment.get();
        if (!parentComment.getNote().getNoteId().equals(noteId)) {
            throw new CommentNotFoundException("评论与笔记不匹配");
        }

        Note note = optionalNote.get();
        /*User user = userService.getCurrentUser();*/
        UserDto user = userServiceClient.getCurrentUser().getBody();
        Comment reply = commentService.replyToComment(note, parentComment, user, content);

        // 将Comment对象转换成DTO对象
        CommentResponseDto commentResponseDto = commentService.toCommentResponseDto(reply);

        return ResponseEntity.ok(commentResponseDto);
    }

    // 处理删除评论（包含回复）的请求
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable("commentId") Long commentId) {
        // 判定评论是否存在
        Optional<Comment> optionalComment = commentService.findCommentById(commentId);
        if (!optionalComment.isPresent()) {
            throw new CommentNotFoundException("");
        }

        // 判定评论是否是自己的
        Comment comment = optionalComment.get();
        /*User user = userService.getCurrentUser();*/
        UserDto user = userServiceClient.getCurrentUser().getBody();

        // 将用户对象改为了用户ID
        /*if (!comment.getUser().getUserId().equals(user.getUserId())) {*/
        if (!comment.getUserId().equals(user.getUserId())) {
            throw new CommentNotFoundException("无权删除他人的评论");
        }

        commentService.deleteComment(comment);

        return ResponseEntity.noContent().build();
    }
}
