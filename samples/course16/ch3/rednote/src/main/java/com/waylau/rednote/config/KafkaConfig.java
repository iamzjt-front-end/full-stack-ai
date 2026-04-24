package com.waylau.rednote.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.kafka.config.TopicBuilder;

/**
 * KafkaConfig Kafka配置
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/09/05
 **/
public class KafkaConfig {
    /**
     * 用户行为的主题
     */
    public static final String TOPIC_USER_ACTION = "user-action";

    /**
     * 创建用户行为的主题
     */
    public NewTopic userActionTopic() {
        return TopicBuilder.name(TOPIC_USER_ACTION)
                // 根据浏览设置分区数
                .partitions(3)
                // 数据保留1天
                .config(TopicConfig.RETENTION_MS_CONFIG, "86400000")
                .build();
    }
}
