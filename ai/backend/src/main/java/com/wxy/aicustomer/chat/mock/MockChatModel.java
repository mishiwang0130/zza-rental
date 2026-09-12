package com.wxy.aicustomer.chat.mock;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 本地 Mock 模型：不调用任何大模型，用于没有 API Key 时联调前端与 SSE 链路。
 */
public class MockChatModel implements ChatModel {

    private static final int CHUNK_SIZE = 12;
    private static final Duration CHUNK_INTERVAL = Duration.ofMillis(30);

    @Override
    public ChatResponse call(Prompt prompt) {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(reply(prompt)))));
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        List<String> chunks = split(reply(prompt));
        return Flux.fromIterable(chunks)
                .delayElements(CHUNK_INTERVAL)
                .map(chunk -> new ChatResponse(List.of(new Generation(new AssistantMessage(chunk)))));
    }

    private String reply(Prompt prompt) {
        Message last = prompt.getLastUserOrToolResponseMessage();
        String question = last == null ? "" : last.getText();
        return """
                【本地 Mock 模型】已收到你的问题：%s

                当前使用 app.chat.provider=mock，没有调用真实大模型，只用于验证前端交互与 SSE 流式链路。
                配置 AI_API_KEY 后把 app.chat.provider 改回 openai，即可接入真实模型。
                """.formatted(question);
    }

    private List<String> split(String text) {
        List<String> chunks = new ArrayList<>();
        for (int index = 0; index < text.length(); index += CHUNK_SIZE) {
            chunks.add(text.substring(index, Math.min(text.length(), index + CHUNK_SIZE)));
        }
        return chunks;
    }
}
