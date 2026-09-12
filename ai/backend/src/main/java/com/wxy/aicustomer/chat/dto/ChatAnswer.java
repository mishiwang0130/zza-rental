package com.wxy.aicustomer.chat.dto;

import com.wxy.aicustomer.rag.SourceRef;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 非流式回答，便于 Swagger 调试和脚本化回归。
 */
@Schema(description = "聊天回答")
public record ChatAnswer(String conversationId, String answer, List<SourceRef> sources) {
}
