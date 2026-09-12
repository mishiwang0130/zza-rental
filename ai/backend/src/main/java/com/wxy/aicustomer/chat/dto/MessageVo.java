package com.wxy.aicustomer.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 会话历史中的一条消息。
 */
@Schema(description = "会话消息")
public record MessageVo(String role, String content) {
}
