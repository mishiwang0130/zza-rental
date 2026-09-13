package com.wxy.aicustomer.knowledge.entity;

import com.wxy.aicustomer.knowledge.enums.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 知识库文档记录。只描述“上传了什么文档、切片多少、是否入库”，正文存在向量库与原始文件中。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeDocument {

    private String id;

    private String fileName;

    private String contentType;

    private String category;

    /** 适用城市标签；平台级通用文档为"通用" */
    private String city;

    private long size;

    /** 原始文件存储键，用于重建索引 */
    private String storageKey;

    private int chunkCount;

    private DocumentStatus status;

    private String errorMessage;

    private Instant createdAt;

    private Instant updatedAt;
}
