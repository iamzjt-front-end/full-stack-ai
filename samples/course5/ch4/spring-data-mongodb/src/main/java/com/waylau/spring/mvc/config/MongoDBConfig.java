package com.waylau.spring.mvc.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * MongoDBConfig MongoDB配置
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/13
 **/
@Configuration
// 启用MongoDB资源库
@EnableMongoRepositories(basePackages = "com.waylau.spring.mvc.repository")
public class MongoDBConfig extends AbstractMongoClientConfiguration {
    @Override
    protected String getDatabaseName() {
        return "springdata";
    }

    @Override
    @Bean
    public MongoClient mongoClient() {
        // 连接MongoDB服务器（默认是在localhost:27017）
        return MongoClients.create("mongodb://localhost:27017");
    }
}
