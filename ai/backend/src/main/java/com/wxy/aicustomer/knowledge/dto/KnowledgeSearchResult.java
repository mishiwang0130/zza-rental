package com.wxy.aicustomer.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 检索命中的切片。
 */
@Schema(description = "检索命中的知识切片")
public record KnowledgeSearchResult(
        String documentId,
        String fileName,
        String category,
        Integer chunkIndex,
        Double score,
        String content
) {
}
