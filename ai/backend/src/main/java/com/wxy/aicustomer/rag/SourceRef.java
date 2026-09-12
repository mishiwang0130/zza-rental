package com.wxy.aicustomer.rag;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 回答引用的知识库来源，前端据此展示参考资料。
 */
@Schema(description = "知识库来源")
public record SourceRef(
        String documentId,
        String fileName,
        String category,
        Double score,
        String snippet
) {
}
