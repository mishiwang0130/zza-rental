package com.wxy.aicustomer.config;

import com.wxy.aicustomer.tool.RoomTools;
import com.wxy.aicustomer.tool.ViewingTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.template.NoOpTemplateRenderer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * ChatClient 装配：系统提示词 + 多轮 Memory + Function Tool。
 */
@Slf4j
@Configuration
public class AiChatClientConfiguration {

    private static final String FALLBACK_SYSTEM_PROMPT = """
            你是公寓租赁平台的智能客服，负责回答租房咨询、公寓规则和房源相关问题。
            请使用简体中文、语气友好简洁；不确定的信息要明确说明，不要编造价格和合同条款。
            """;

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder,
                                 ChatMemory chatMemory,
                                 AppProperties properties,
                                 ResourceLoader resourceLoader,
                                 ObjectProvider<RoomTools> roomTools,
                                 ObjectProvider<ViewingTools> viewingTools) {
        List<Object> tools = new ArrayList<>();
        roomTools.ifAvailable(tools::add);
        viewingTools.ifAvailable(tools::add);

        ChatClient.Builder clientBuilder = builder
                .defaultSystem(loadSystemPrompt(resourceLoader, properties))
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultTemplateRenderer(new NoOpTemplateRenderer());
        if (!tools.isEmpty()) {
            clientBuilder = clientBuilder.defaultTools(tools.toArray());
        }
        log.info("ChatClient 初始化完成，注册 Tool {} 个", tools.size());
        return clientBuilder.build();
    }

    private String loadSystemPrompt(ResourceLoader resourceLoader, AppProperties properties) {
        Resource resource = resourceLoader.getResource(properties.getChat().getSystemPromptLocation());
        if (!resource.exists()) {
            log.warn("未找到系统提示词 {}，使用内置默认提示词", properties.getChat().getSystemPromptLocation());
            return FALLBACK_SYSTEM_PROMPT;
        }
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("系统提示词读取失败：" + properties.getChat().getSystemPromptLocation(), e);
        }
    }
}
