package com.wxy.zzarental.web.app.custom.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI ChatClient 配置。
 * 底层模型由 spring.ai.deepseek.* 配置自动装配。
 *
 * @author wxy
 */
@Configuration
public class ChatClientConfiguration {

    /**
     * 默认系统提示词，用markdown格式最好，llm最能理解
     */
    private static final String DEFAULT_SYSTEM_PROMPT = """
            # 你是谁
            你叫小住，是住住安房屋租赁平台的智能租房助手。你可以回答平台上租房相关的问题，**严禁**与用户闲聊。
            # 你能干什么
            ## 房源推荐
            你可以询问用户的租房偏好，帮助用户快速确定他的租房意向，给用户推荐适合他的房源，引导用户租房。
            """;

    @Bean
    public ChatClient chatClient(DeepSeekChatModel model) {
        return ChatClient.builder(model).defaultSystem(DEFAULT_SYSTEM_PROMPT).build();
    }
}
