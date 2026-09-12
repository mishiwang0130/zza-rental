package com.wxy.aicustomer.knowledge.storage;

import org.springframework.core.io.Resource;

import java.io.InputStream;

/**
 * 知识文档原始文件存储扩展点：本地磁盘或 MinIO。
 */
public interface FileStorageService {

    /**
     * 保存原始文件，返回存储键。
     */
    String store(String documentId, String fileName, InputStream content);

    /**
     * 按存储键读取原始文件，用于重建索引。
     */
    Resource load(String storageKey);

    void delete(String storageKey);
}
