package com.wxy.aicustomer.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 访客聊天请求。conversationId 为空表示新会话，由服务端生成后通过 meta 事件回传。
 */
@Schema(description = "访客聊天请求")
public record ChatRequest(
        @Schema(description = "浏览器本地生成的匿名访客标识", example = "8f14e45f-ea0b-4b1b-9c1e-2f4d1d2a9b31")
        @NotBlank(message = "不能为空")
        @Size(max = 64, message = "长度不能超过 64")
        String visitorId,

        @Schema(description = "会话标识；为空表示开启新会话", example = "1c9f9c4e-2b3d-4a5f-8c7e-9d0a1b2c3d4e")
        @Size(max = 64, message = "长度不能超过 64")
        String conversationId,

        @Schema(description = "访客消息内容", example = "两室一厅大概多少钱？")
        @NotBlank(message = "不能为空")
        @Size(max = 2000, message = "长度不能超过 2000")
        String message,

        @Schema(description = "访客选择的城市标签；为空表示不按城市过滤", example = "武汉")
        @Size(max = 32, message = "长度不能超过 32")
        String city
) {
}
