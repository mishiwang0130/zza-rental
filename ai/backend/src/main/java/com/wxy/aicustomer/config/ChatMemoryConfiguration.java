package com.wxy.aicustomer.config;

import com.wxy.aicustomer.memory.MessageCodec;
import com.wxy.aicustomer.memory.RedisChatMemoryRepository;
import com.wxy.zzarental.common.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 多轮会话 Memory 装配。
 *
 * <p>默认走 Redis；app.memory.type=memory 时退化为进程内实现，方便没有中间件的本地调试。
 */
@Slf4j
@Configuration
public class ChatMemoryConfiguration {

    @Bean
    @ConditionalOnProperty(name = "app.memory.type", havingValue = "redis", matchIfMissing = true)
    public ChatMemoryRepository redisChatMemoryRepository(RedisUtil redisUtil,
                                                         MessageCodec messageCodec,
                                                         AppProperties properties) {
        log.info("会话 Memory 使用 Redis，TTL={}", properties.getMemory().getTtl());
        return new RedisChatMemoryRepository(redisUtil, messageCodec, properties.getMemory().getTtl());
    }

    @Bean
    @ConditionalOnProperty(name = "app.memory.type", havingValue = "memory")
    public ChatMemoryRepository inMemoryChatMemoryRepository() {
        log.warn("会话 Memory 使用进程内实现，重启后历史会话会丢失");
        return new InMemoryChatMemoryRepository();
    }

    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository, AppProperties properties) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(properties.getChat().getMemoryWindowSize())
                .build();
    }
}
