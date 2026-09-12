package com.wxy.aicustomer.chat.service.impl;

import com.wxy.aicustomer.chat.dto.ChatAnswer;
import com.wxy.aicustomer.chat.dto.ChatEvent;
import com.wxy.aicustomer.chat.dto.ChatRequest;
import com.wxy.aicustomer.chat.dto.ChatStreamPayload;
import com.wxy.aicustomer.chat.dto.MessageVo;
import com.wxy.aicustomer.chat.service.ChatService;
import com.wxy.aicustomer.config.AppProperties;
import com.wxy.aicustomer.rag.RagService;
import com.wxy.aicustomer.rag.RetrievalResult;
import com.wxy.zzarental.common.exception.ZZAException;
import com.wxy.zzarental.common.result.ResultCodeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 聊天主流程：RAG 检索 → 拼接提示词 → 模型流式输出 → SSE 事件。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private static final String ERROR_CODE_CHAT_FAILED = "CHAT_FAILED";

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final RagService ragService;
    private final AppProperties properties;

    @Override
    public Flux<ChatEvent> stream(ChatRequest request) {
        String conversationId = resolveConversationId(request.conversationId());
        String question = normalize(request.message());
        return Flux.defer(() -> {
                    long startedAt = System.nanoTime();
                    RetrievalResult retrieval = ragService.retrieve(question);
                    return Flux.concat(
                            Flux.just(ChatEvent.of(ChatEvent.EVENT_META,
                                    new ChatStreamPayload.Meta(conversationId, request.visitorId()))),
                            streamAnswer(conversationId, question, retrieval),
                            Flux.just(
                                    ChatEvent.of(ChatEvent.EVENT_SOURCES,
                                            new ChatStreamPayload.Sources(retrieval.sources())),
                                    ChatEvent.of(ChatEvent.EVENT_DONE,
                                            new ChatStreamPayload.Done(conversationId, elapsedMillis(startedAt)))));
                })
                // 检索与模型调用都是网络阻塞操作，放到弹性线程池，避免占用 Servlet 线程
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(ex -> {
                    log.error("会话 {} 回答失败", conversationId, ex);
                    return Flux.just(ChatEvent.of(ChatEvent.EVENT_ERROR,
                            new ChatStreamPayload.Error(ERROR_CODE_CHAT_FAILED, "AI 服务暂时不可用，请稍后重试")));
                });
    }

    @Override
    public ChatAnswer ask(ChatRequest request) {
        String conversationId = resolveConversationId(request.conversationId());
        String question = normalize(request.message());
        RetrievalResult retrieval = ragService.retrieve(question);
        String answer = chatClient.prompt()
                .user(ragService.buildUserPrompt(question, retrieval))
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
        return new ChatAnswer(conversationId, answer, retrieval.sources());
    }

    @Override
    public List<MessageVo> history(String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            throw new ZZAException(ResultCodeEnum.PARAM_ERROR.getCode(), "conversationId 不能为空");
        }
        return chatMemory.get(conversationId).stream()
                .map(this::toMessageVo)
                .toList();
    }

    @Override
    public void clear(String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            throw new ZZAException(ResultCodeEnum.PARAM_ERROR.getCode(), "conversationId 不能为空");
        }
        chatMemory.clear(conversationId);
        log.info("已清空会话 {}", conversationId);
    }

    private Flux<ChatEvent> streamAnswer(String conversationId, String question, RetrievalResult retrieval) {
        return chatClient.prompt()
                .user(ragService.buildUserPrompt(question, retrieval))
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content()
                .map(delta -> ChatEvent.of(ChatEvent.EVENT_DELTA, new ChatStreamPayload.Delta(delta)));
    }

    private MessageVo toMessageVo(Message message) {
        return new MessageVo(message.getMessageType().getValue(), message.getText());
    }

    private String resolveConversationId(String conversationId) {
        return StringUtils.hasText(conversationId) ? conversationId.trim() : UUID.randomUUID().toString();
    }

    private String normalize(String message) {
        String trimmed = message == null ? "" : message.trim();
        if (trimmed.isEmpty()) {
            throw new ZZAException(ResultCodeEnum.PARAM_ERROR.getCode(), "消息内容不能为空");
        }
        int maxLength = properties.getChat().getMaxMessageLength();
        if (trimmed.length() > maxLength) {
            throw new ZZAException(ResultCodeEnum.PARAM_ERROR.getCode(), "消息内容超过 " + maxLength + " 个字符");
        }
        return trimmed;
    }

    private double elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / (double) TimeUnit.MILLISECONDS.toNanos(1);
    }
}
