package com.wxy.aicustomer.knowledge.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wxy.aicustomer.knowledge.entity.KnowledgeDocument;
import com.wxy.aicustomer.knowledge.mapper.KnowledgeDocumentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * 知识文档记录存 MySQL（表 ai_knowledge_document）。
 *
 * <p>文档记录是业务数据，需要长期保存、可查询、可审计，所以放 MySQL；
 * Redis 只用来存会话记忆这类带 TTL 的缓存。
 */
@Repository
@RequiredArgsConstructor
public class MysqlKnowledgeDocumentRepository implements KnowledgeDocumentRepository {

    private final KnowledgeDocumentMapper knowledgeDocumentMapper;

    @Override
    public void save(KnowledgeDocument document) {
        // 主键由服务端生成（UUID），所以这里判断是新增还是更新
        if (knowledgeDocumentMapper.selectById(document.getId()) == null) {
            knowledgeDocumentMapper.insert(document);
        } else {
            knowledgeDocumentMapper.updateById(document);
        }
    }

    @Override
    public Optional<KnowledgeDocument> findById(String id) {
        return StringUtils.hasText(id)
                ? Optional.ofNullable(knowledgeDocumentMapper.selectById(id))
                : Optional.empty();
    }

    @Override
    public List<KnowledgeDocument> findAll() {
        // 按创建时间倒序，最新的文档排在最前
        return knowledgeDocumentMapper.selectList(Wrappers.<KnowledgeDocument>lambdaQuery()
                .orderByDesc(KnowledgeDocument::getCreateTime));
    }

    @Override
    public void deleteById(String id) {
        if (StringUtils.hasText(id)) {
            // 实体上有 @TableLogic，这里是逻辑删除（is_delete = 1）
            knowledgeDocumentMapper.deleteById(id);
        }
    }
}
