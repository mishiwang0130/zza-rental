package com.wxy.aicustomer.knowledge.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxy.aicustomer.common.constant.AiRedisKeys;
import com.wxy.aicustomer.knowledge.entity.KnowledgeDocument;
import com.wxy.zzarental.common.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 文档记录存 Redis Hash：field = documentId，value = JSON。Redis 操作复用 common 的 RedisUtil。
 */
@Slf4j
@RequiredArgsConstructor
public class RedisKnowledgeDocumentRepository implements KnowledgeDocumentRepository {

    private final RedisUtil redisUtil;
    private final ObjectMapper objectMapper;

    @Override
    public void save(KnowledgeDocument document) {
        try {
            redisUtil.hashSet(AiRedisKeys.KNOWLEDGE_DOCUMENT_HASH, document.getId(),
                    objectMapper.writeValueAsString(document));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("知识文档记录序列化失败", e);
        }
    }

    @Override
    public Optional<KnowledgeDocument> findById(String id) {
        String value = redisUtil.hashGet(AiRedisKeys.KNOWLEDGE_DOCUMENT_HASH, id);
        return value == null ? Optional.empty() : Optional.of(deserialize(value));
    }

    @Override
    public List<KnowledgeDocument> findAll() {
        Map<String, String> entries = redisUtil.hashGetAll(AiRedisKeys.KNOWLEDGE_DOCUMENT_HASH);
        return entries.values().stream()
                .map(this::deserialize)
                .sorted(Comparator.comparing(KnowledgeDocument::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Override
    public void deleteById(String id) {
        redisUtil.hashDelete(AiRedisKeys.KNOWLEDGE_DOCUMENT_HASH, id);
    }

    private KnowledgeDocument deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, KnowledgeDocument.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("知识文档记录反序列化失败", e);
        }
    }
}
