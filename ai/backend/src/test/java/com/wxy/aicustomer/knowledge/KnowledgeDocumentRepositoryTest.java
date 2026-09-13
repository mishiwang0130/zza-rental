package com.wxy.aicustomer.knowledge;

import com.wxy.aicustomer.knowledge.entity.KnowledgeDocument;
import com.wxy.aicustomer.knowledge.enums.DocumentStatus;
import com.wxy.aicustomer.knowledge.repository.KnowledgeDocumentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 知识文档记录持久化测试。
 *
 * <p>test profile 用 H2 内存库跑，SQL 与 MySQL 一致，因此能覆盖实体映射、
 * 逻辑删除、数据库默认时间等真正落到数据库上的行为。
 */
@SpringBootTest
@ActiveProfiles("test")
class KnowledgeDocumentRepositoryTest {

    private static final String DOCUMENT_ID = "doc-test-0001";

    @Autowired
    private KnowledgeDocumentRepository documentRepository;

    @Test
    void shouldInsertUpdateQueryAndLogicDelete() {
        KnowledgeDocument document = KnowledgeDocument.builder()
                .id(DOCUMENT_ID)
                .fileName("武汉_公寓租房管理规定.md")
                .contentType("text/markdown")
                .category("租赁规定")
                .city("武汉")
                .fileSize(128)
                .storageKey("documents/" + DOCUMENT_ID + "/武汉_公寓租房管理规定.md")
                .chunkCount(0)
                .status(DocumentStatus.PENDING)
                .build();

        documentRepository.save(document);

        KnowledgeDocument inserted = documentRepository.findById(DOCUMENT_ID).orElseThrow();
        assertThat(inserted.getFileName()).isEqualTo("武汉_公寓租房管理规定.md");
        assertThat(inserted.getCity()).isEqualTo("武汉");
        assertThat(inserted.getStatus()).isEqualTo(DocumentStatus.PENDING);
        // create_time 由数据库默认值填充
        assertThat(inserted.getCreateTime()).isNotNull();

        // 再次 save 走 update 分支：状态与切片数更新，失败原因被清空
        inserted.setChunkCount(8);
        inserted.setStatus(DocumentStatus.INDEXED);
        inserted.setErrorMessage(null);
        documentRepository.save(inserted);

        KnowledgeDocument updated = documentRepository.findById(DOCUMENT_ID).orElseThrow();
        assertThat(updated.getChunkCount()).isEqualTo(8);
        assertThat(updated.getStatus()).isEqualTo(DocumentStatus.INDEXED);
        assertThat(updated.getErrorMessage()).isNull();

        assertThat(documentRepository.findAll())
                .extracting(KnowledgeDocument::getId)
                .contains(DOCUMENT_ID);

        // 逻辑删除后查不到（@TableLogic 自动拼 is_delete = 0）
        documentRepository.deleteById(DOCUMENT_ID);
        assertThat(documentRepository.findById(DOCUMENT_ID)).isEmpty();
        assertThat(documentRepository.findAll())
                .extracting(KnowledgeDocument::getId)
                .doesNotContain(DOCUMENT_ID);
    }

    @Test
    void shouldReturnEmptyForUnknownOrBlankId() {
        assertThat(documentRepository.findById("no-such-document")).isEmpty();
        assertThat(documentRepository.findById("  ")).isEmpty();
        assertThat(documentRepository.findById(null)).isEmpty();
    }
}
