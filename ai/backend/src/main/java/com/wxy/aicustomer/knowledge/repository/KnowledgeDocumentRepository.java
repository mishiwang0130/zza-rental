package com.wxy.aicustomer.knowledge.repository;

import com.wxy.aicustomer.knowledge.entity.KnowledgeDocument;

import java.util.List;
import java.util.Optional;

/**
 * 知识文档记录存储。
 *
 * <p>当前实现是 {@link MysqlKnowledgeDocumentRepository}，记录落在 MySQL 表 ai_knowledge_document；
 * 文档记录属于业务数据，不应该放 Redis（Redis 只存会话记忆这类缓存）。
 */
public interface KnowledgeDocumentRepository {

    void save(KnowledgeDocument document);

    Optional<KnowledgeDocument> findById(String id);

    List<KnowledgeDocument> findAll();

    void deleteById(String id);
}
