package com.wxy.aicustomer.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 知识文档记录视图。
 */
@Schema(description = "知识文档记录")
public record DocumentVo(
        String id,
        String fileName,
        String category,
        String city,
        String contentType,
        long size,
        int chunkCount,
        String status,
        String errorMessage,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {
}
