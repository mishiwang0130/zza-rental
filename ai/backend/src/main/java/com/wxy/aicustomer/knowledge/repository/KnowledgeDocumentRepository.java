package com.wxy.aicustomer.knowledge.repository;

import com.wxy.aicustomer.knowledge.entity.KnowledgeDocument;

import java.util.List;
import java.util.Optional;

/**
 * 知识文档记录存储。当前提供 Redis 与进程内两种实现，后续可换成 MySQL。
 */
public interface KnowledgeDocumentRepository {

    void save(KnowledgeDocument document);

    Optional<KnowledgeDocument> findById(String id);

    List<KnowledgeDocument> findAll();

    void deleteById(String id);
}
