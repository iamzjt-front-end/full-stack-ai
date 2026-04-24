package com.waylau.rednote.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

/**
 * MongoConfig MongoDB配置
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/09/06
 **/
@Configuration
public class MongoConfig {
    public static String STATIC_PATH_PREFIX = "/file/";

    @Autowired
    private MongoClient mongoClient;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    // 配置GridFSBucket Bean
    @Bean
    public GridFSBucket gridFSBucket() {
        return GridFSBuckets.create(mongoClient.getDatabase(databaseName));
    }

    // 配置GridFS模板
    @Bean
    public GridFsTemplate gridFsTemplate(MongoDatabaseFactory mongoDatabaseFactory,
                                         MongoConverter mongoConverter) {
        return new GridFsTemplate(mongoDatabaseFactory, mongoConverter);
    }

}
