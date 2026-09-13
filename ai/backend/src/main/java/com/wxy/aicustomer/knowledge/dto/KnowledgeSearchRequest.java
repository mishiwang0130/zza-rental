package com.wxy.aicustomer.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 语义检索调试请求。
 */
@Schema(description = "知识库检索调试请求")
public record KnowledgeSearchRequest(
        @Schema(description = "检索问题", example = "退租需要提前多久申请？")
        @NotBlank(message = "不能为空")
        @Size(max = 500, message = "长度不能超过 500")
        String query,

        @Schema(description = "召回条数，默认取配置值", example = "5")
        Integer topK,

        @Schema(description = "相似度阈值 0~1，默认取配置值", example = "0.5")
        Double similarityThreshold,

        @Schema(description = "城市标签；填写后只召回该城市与通用文档，不填则不按城市过滤", example = "武汉")
        @Size(max = 32, message = "长度不能超过 32")
        String city
) {
}
