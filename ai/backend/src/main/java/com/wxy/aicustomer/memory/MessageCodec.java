package com.wxy.aicustomer.memory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 会话消息与 Redis 字符串之间的编解码。
 *
 * <p>不直接序列化 Spring AI 的 Message 对象，避免上游类结构变化影响缓存兼容性。
 */
@Component
public class MessageCodec {

    private final ObjectMapper objectMapper;

    public MessageCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String encode(Message message) {
        StoredMessage stored = new StoredMessage(message.getMessageType().getValue(), message.getText());
        try {
            return objectMapper.writeValueAsString(stored);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("会话消息序列化失败", e);
        }
    }

    public Message decode(String payload) {
        StoredMessage stored;
        try {
            stored = objectMapper.readValue(payload, StoredMessage.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("会话消息反序列化失败", e);
        }
        String text = stored.text() == null ? "" : stored.text();
        MessageType type = MessageType.fromValue(stored.type());
        return switch (type) {
            case USER -> new UserMessage(text);
            case SYSTEM -> new SystemMessage(text);
            case ASSISTANT -> new AssistantMessage(text);
            case TOOL -> ToolResponseMessage.builder()
                    .responses(List.of(new ToolResponseMessage.ToolResponse("tool", "tool", text)))
                    .build();
        };
    }

    public record StoredMessage(String type, String text) {
    }
}
