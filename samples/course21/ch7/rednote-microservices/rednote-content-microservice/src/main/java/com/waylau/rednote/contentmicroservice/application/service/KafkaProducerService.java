package com.waylau.rednote.contentmicroservice.application.service;

import com.waylau.rednote.contentmicroservice.application.dto.UserActionEvent;

/**
 * KafkaProducerService Kafka生产者服务
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/09/05
 **/
public interface KafkaProducerService {
    /**
     * 发送用户行为事件
     *
     * @param userActionEvent
     */
    void sendUserActionEvent(UserActionEvent userActionEvent);
}
