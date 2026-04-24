package com.waylau.rednote.contentmicroservice.application.service;

import com.waylau.rednote.contentmicroservice.infrastructure.config.KafkaConfig;
import com.waylau.rednote.contentmicroservice.application.dto.BrowseEvent;
import com.waylau.rednote.contentmicroservice.application.dto.CommentEvent;
import com.waylau.rednote.contentmicroservice.application.dto.LikeEvent;
import com.waylau.rednote.contentmicroservice.application.dto.UserActionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * RealTimeAnalysisServiceImpl 实时分析服务
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/09/05
 **/
@Service
public class RealTimeAnalysisServiceImpl implements RealTimeAnalysisService {

    @Autowired
    private NoteService noteService;

    @Override
    @KafkaListener(topics = KafkaConfig.TOPIC_USER_ACTION)
    public void analyzeHotNotes(@Payload UserActionEvent userActionEvent) {
        if (userActionEvent instanceof BrowseEvent) {
            BrowseEvent browseEvent = (BrowseEvent) userActionEvent;

            Long noteId = browseEvent.getNoteId();
            long browseTime = browseEvent.getBrowseTime();

            // 调用服务增加笔记访问量
            noteService.incrementBrowseCount(noteId);

            // 调用服务增加笔记浏览时长
            noteService.incrementBrowseTime(noteId, browseTime);
        } else if (userActionEvent instanceof LikeEvent) {
            // TODO
        } else if (userActionEvent instanceof CommentEvent) {
            // TODO
        }
    }
}
