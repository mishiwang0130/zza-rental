package com.wxy.aicustomer.chat.controller;

import com.wxy.aicustomer.chat.dto.ChatAnswer;
import com.wxy.aicustomer.chat.dto.ChatRequest;
import com.wxy.aicustomer.chat.dto.MessageVo;
import com.wxy.aicustomer.chat.service.ChatService;
import com.wxy.zzarental.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 访客聊天接口，匿名可用。
 */
@Tag(name = "访客聊天", description = "匿名访客聊天，支持 SSE 流式输出")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "流式聊天（SSE）",
            description = "事件顺序：meta → delta（多条）→ sources → done；异常时返回 error 事件")
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Object>> stream(@Valid @RequestBody ChatRequest request) {
        // Service 返回的是业务事件（ChatEvent），这里只做一层适配：
        // 把事件的 name 映射成 SSE 的 event 字段、data 映射成 data 字段，前端按事件名分发处理。
        // produces=text/event-stream 让 Spring MVC 以异步方式把 Flux 逐条写回响应，
        // 所以这个接口"返回"之后请求线程就释放了，不会一直挂着一个 Servlet 线程
        return chatService.stream(request)
                .map(event -> ServerSentEvent.builder(event.data()).event(event.name()).build());
    }

    @Operation(summary = "非流式聊天", description = "用于 Swagger 调试与脚本化验证")
    @PostMapping
    public Result<ChatAnswer> ask(@Valid @RequestBody ChatRequest request) {
        return Result.ok(chatService.ask(request));
    }

    @Operation(summary = "查询会话历史", description = "读取会话 Memory 中保留的最近若干轮消息")
    @GetMapping("/conversations/{conversationId}/messages")
    public Result<List<MessageVo>> history(@PathVariable String conversationId) {
        return Result.ok(chatService.history(conversationId));
    }

    @Operation(summary = "清空会话", description = "对应前端“新对话”，清空该 conversationId 的历史")
    @DeleteMapping("/conversations/{conversationId}")
    public Result<Void> clear(@PathVariable String conversationId) {
        chatService.clear(conversationId);
        return Result.ok();
    }
}
