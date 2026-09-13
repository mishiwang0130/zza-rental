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
 *
 * <p><b>一次提问的完整动作</b>：
 * <pre>
 *   stream(request)
 *     1) 定会话：没有 conversationId 就生成一个 UUID（前端下次带上，实现多轮）
 *     2) 校验问题：非空 + 长度不超过 max-message-length
 *     3) 检索知识库（RagService，失败会降级为空结果，不阻断回答）
 *     4) 拼提示词后调模型，逐 token 转成 delta 事件推给前端
 *     5) 收尾推 sources（参考资料）与 done（会话 id + 耗时）；出错推 error
 * </pre>
 *
 * <p><b>为什么返回 Flux 而不是直接写 response</b>：Controller 把它映射成 SSE，
 * 浏览器能边收边渲染，用户不用等完整回答生成完（首字延迟从"秒级"降到"百毫秒级"）。
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
        // Flux.defer：整个执行体要等到真正有人订阅时才跑（每次请求执行一次），
        // 也保证"生成部分"（检索、拼提示词）发生在被调度的线程上
        return Flux.defer(() -> {
                    long startedAt = System.nanoTime();
                    // 先做检索：拿到片段后才有参考资料可拼进提示词，因此它在模型调用之前
                    RetrievalResult retrieval = ragService.retrieve(question, request.city());
                    // Flux.concat 严格按顺序拼接三个数据源，保证前端收到的事件顺序固定：
                    // meta（会话 id）→ delta（正文增量，多条）→ sources → done
                    return Flux.concat(
                            Flux.just(ChatEvent.of(ChatEvent.EVENT_META,
                                    new ChatStreamPayload.Meta(conversationId, request.visitorId()))),
                            streamAnswer(conversationId, question, request.city(), retrieval),
                            Flux.just(
                                    ChatEvent.of(ChatEvent.EVENT_SOURCES,
                                            new ChatStreamPayload.Sources(retrieval.sources())),
                                    ChatEvent.of(ChatEvent.EVENT_DONE,
                                            new ChatStreamPayload.Done(conversationId, elapsedMillis(startedAt)))));
                })
                // 检索（调 Embedding）与模型调用都是阻塞式网络 IO，切到弹性线程池执行，
                // 避免占住 Servlet 请求线程，也避免阻塞 reactor 的少量事件循环线程
                .subscribeOn(Schedulers.boundedElastic())
                // 兜底：任何未捕获异常都转成 error 事件推给前端，而不是直接掐断流。
                // 注意不把异常堆栈/内部信息透给前端，只给一句友好提示
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
        RetrievalResult retrieval = ragService.retrieve(question, request.city());
        String answer = chatClient.prompt()
                .user(ragService.buildUserPrompt(question, request.city(), retrieval))
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

    private Flux<ChatEvent> streamAnswer(String conversationId, String question, String city,
                                         RetrievalResult retrieval) {
        return chatClient.prompt()
                // 把"城市 + 问题 + 参考知识库资料"拼成本轮 user 消息；
                // 历史消息由下面的 Memory 顾问自动补上，不需要在这里手动拼
                .user(ragService.buildUserPrompt(question, city, retrieval))
                // 把 conversationId 传给 Memory 顾问：它据此从 Redis 取该会话最近的消息，
                // 并在本轮结束后把新的一问一答写回去——这就是"多轮对话"的实现位置
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content()
                // 模型每产出一小段文本就映射成一个 delta 事件（真正的流式体验）
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
