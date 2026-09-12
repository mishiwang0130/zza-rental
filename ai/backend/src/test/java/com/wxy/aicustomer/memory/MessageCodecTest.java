package com.wxy.aicustomer.memory;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 会话消息编解码测试，保证 Redis 中的历史消息能还原成 Spring AI 的消息对象。
 */
class MessageCodecTest {

    private final MessageCodec codec = new MessageCodec(new ObjectMapper());

    @Test
    void shouldRoundTripAllSupportedMessageTypes() {
        assertRoundTrip(new UserMessage("有哪些空房？"), MessageType.USER, "有哪些空房？");
        assertRoundTrip(new AssistantMessage("可以看看这两套"), MessageType.ASSISTANT, "可以看看这两套");
        assertRoundTrip(new SystemMessage("你是客服"), MessageType.SYSTEM, "你是客服");
    }

    @Test
    void shouldKeepTextWithSpecialCharacters() {
        String text = "{\"预算\":3000,\"户型\":\"两室\"}";
        Message decoded = codec.decode(codec.encode(new UserMessage(text)));
        assertThat(decoded.getText()).isEqualTo(text);
    }

    private void assertRoundTrip(Message message, MessageType expectedType, String expectedText) {
        Message decoded = codec.decode(codec.encode(message));
        assertThat(decoded.getMessageType()).isEqualTo(expectedType);
        assertThat(decoded.getText()).isEqualTo(expectedText);
    }
}
