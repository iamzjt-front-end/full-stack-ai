package com.waylau.rednote.controller;

import com.waylau.rednote.dto.BrowseEvent;
import com.waylau.rednote.service.KafkaProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * LogController 日志收集控制器
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/09/05
 **/
@RestController
@RequestMapping("/log")
public class LogController {

    @Autowired
    private KafkaProducerService kafkaProducerService;

    // 记录浏览事件
    @PostMapping("/browse")
    public ResponseEntity<Void> logBrowseEvent(@RequestBody BrowseEvent event) {
        // 设置事件戳
        event.setTimestamp(LocalDateTime.now());

        // 发送到Kafka
        kafkaProducerService.sendUserActionEvent(event);

        return ResponseEntity.ok().build();
    }
}
