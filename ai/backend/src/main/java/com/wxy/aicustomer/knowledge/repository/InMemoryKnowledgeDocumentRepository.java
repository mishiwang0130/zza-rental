package com.wxy.aicustomer.knowledge.repository;

import com.wxy.aicustomer.knowledge.entity.KnowledgeDocument;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 进程内文档记录，仅用于没有 Redis 的本地调试。
 */
public class InMemoryKnowledgeDocumentRepository implements KnowledgeDocumentRepository {

    private final Map<String, KnowledgeDocument> store = new ConcurrentHashMap<>();

    @Override
    public void save(KnowledgeDocument document) {
        store.put(document.getId(), document);
    }

    @Override
    public Optional<KnowledgeDocument> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<KnowledgeDocument> findAll() {
        return store.values().stream()
                .sorted(Comparator.comparing(KnowledgeDocument::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Override
    public void deleteById(String id) {
        store.remove(id);
    }
}
