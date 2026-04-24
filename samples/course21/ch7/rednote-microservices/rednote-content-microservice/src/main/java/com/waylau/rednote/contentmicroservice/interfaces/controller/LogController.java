package com.waylau.rednote.contentmicroservice.interfaces.controller;

import com.waylau.rednote.common.dto.AdminDashboardDto;
import com.waylau.rednote.common.dto.NoteBrowseCountDto;
import com.waylau.rednote.common.dto.NoteBrowseTimeDto;
import com.waylau.rednote.common.interfaces.client.UserServiceClient;
import com.waylau.rednote.contentmicroservice.application.dto.BrowseEvent;
import com.waylau.rednote.contentmicroservice.application.service.CommentService;
import com.waylau.rednote.contentmicroservice.application.service.KafkaProducerService;
import com.waylau.rednote.contentmicroservice.application.service.NoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * LogController 日志收集控制器
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/09/05
 **/
@RestController
@RequestMapping("/log")
public class LogController {
    private static final Logger log = LoggerFactory.getLogger(LogController.class);
    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private NoteService noteService;

    @Autowired
    private CommentService commentService;

    // 记录浏览事件
    @PostMapping("/browse")
    public ResponseEntity<Void> logBrowseEvent(@RequestBody BrowseEvent event) {
        // 设置事件戳
        event.setTimestamp(LocalDateTime.now());

        // 发送到Kafka
        kafkaProducerService.sendUserActionEvent(event);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/dashboard")
    ResponseEntity<AdminDashboardDto> dashboard() {
        log.info("dashboard");
        long userCount = userServiceClient.countUsers().getBody();
        long noteCount = noteService.countNotes();
        long commentCount = commentService.countComments();
        List<NoteBrowseCountDto> noteBrowseCountDtoList = noteService.getNotesByBrowseCount(1, 10);
        List<NoteBrowseTimeDto> noteBrowseTimeDtoList = noteService.getNotesByBrowseTime(1, 10);

        AdminDashboardDto adminDashboardDto = new AdminDashboardDto(userCount, noteCount, commentCount, noteBrowseCountDtoList, noteBrowseTimeDtoList);

        return ResponseEntity.ok(adminDashboardDto);
    }
}
