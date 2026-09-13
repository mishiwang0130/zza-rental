package com.wxy.aicustomer;

import com.wxy.aicustomer.chat.service.ChatService;
import com.wxy.aicustomer.knowledge.service.KnowledgeService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 上下文冒烟测试：test profile 下不依赖 Redis / Qdrant / 真实模型也能启动。
 */
@SpringBootTest
@ActiveProfiles("test")
class AiCustomerApplicationTests {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ChatMemory chatMemory;

    @Autowired
    private ChatService chatService;

    @Autowired
    private KnowledgeService knowledgeService;

    @Test
    void contextLoads() {
        assertThat(chatClient).isNotNull();
        assertThat(chatMemory).isNotNull();
        assertThat(chatService).isNotNull();
        assertThat(knowledgeService).isNotNull();
    }
}
