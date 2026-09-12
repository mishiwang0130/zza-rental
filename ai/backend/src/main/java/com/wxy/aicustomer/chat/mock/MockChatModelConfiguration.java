package com.wxy.aicustomer.chat.mock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * app.chat.provider=mock 时启用本地 Mock 模型（配合 spring.ai.model.chat=none）。
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "app.chat.provider", havingValue = "mock")
public class MockChatModelConfiguration {

    @Bean
    public ChatModel mockChatModel() {
        log.warn("当前使用本地 Mock 聊天模型，不会调用真实大模型");
        return new MockChatModel();
    }
}
