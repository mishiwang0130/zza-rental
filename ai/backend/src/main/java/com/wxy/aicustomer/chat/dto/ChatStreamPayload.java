package com.wxy.aicustomer.chat.dto;

import com.wxy.aicustomer.rag.SourceRef;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * SSE 各类事件的载荷定义，与前端 src/types/chat.ts 一一对应。
 */
public final class ChatStreamPayload {

    private ChatStreamPayload() {
    }

    @Schema(description = "会话元信息，第一个事件")
    public record Meta(String conversationId, String visitorId) {
    }

    @Schema(description = "增量回答片段")
    public record Delta(String content) {
    }

    @Schema(description = "本轮命中的知识库来源")
    public record Sources(List<SourceRef> sources) {
    }

    @Schema(description = "本轮回答结束")
    public record Done(String conversationId, double elapsedMillis) {
    }

    @Schema(description = "异常事件")
    public record Error(String code, String message) {
    }
}
