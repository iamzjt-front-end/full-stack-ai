package com.waylau.rednote.contentmicroservice.application.service;

import com.waylau.rednote.contentmicroservice.infrastructure.config.KafkaConfig;
import com.waylau.rednote.contentmicroservice.application.dto.UserActionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * KafkaProducerServiceImpl Kafka生产者服务
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/09/05
 **/
@Service
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerServiceImpl.class);

    @Autowired
    private KafkaTemplate<String, UserActionEvent> kafkaTemplate;

    @Override
    public void sendUserActionEvent(UserActionEvent userActionEvent) {
        // 使用用户ID作为分区Key，确保相同用户的行为在同一分区内
        String key = String.valueOf(userActionEvent.getUserId());

        // 发送消息到Kafka
        CompletableFuture<SendResult<String, UserActionEvent>> future = kafkaTemplate.send(KafkaConfig.TOPIC_USER_ACTION, key, userActionEvent);

        // 添加回调
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("消息发送成功");
            } else {
                log.error("消息发送失败", ex);
            }
        });
    }
}
