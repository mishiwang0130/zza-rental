package com.wxy.aicustomer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxy.aicustomer.knowledge.repository.InMemoryKnowledgeDocumentRepository;
import com.wxy.aicustomer.knowledge.repository.KnowledgeDocumentRepository;
import com.wxy.aicustomer.knowledge.repository.RedisKnowledgeDocumentRepository;
import com.wxy.zzarental.common.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 知识文档记录存储装配。
 */
@Slf4j
@Configuration
public class KnowledgeConfiguration {

    @Bean
    @ConditionalOnProperty(name = "app.knowledge.repository", havingValue = "redis", matchIfMissing = true)
    public KnowledgeDocumentRepository redisKnowledgeDocumentRepository(RedisUtil redisUtil,
                                                                       ObjectMapper objectMapper) {
        log.info("知识文档记录使用 Redis");
        return new RedisKnowledgeDocumentRepository(redisUtil, objectMapper);
    }

    @Bean
    @ConditionalOnProperty(name = "app.knowledge.repository", havingValue = "memory")
    public KnowledgeDocumentRepository inMemoryKnowledgeDocumentRepository() {
        log.warn("知识文档记录使用进程内实现，重启后记录会丢失");
        return new InMemoryKnowledgeDocumentRepository();
    }
}
