package com.wxy.aicustomer.rag;

import com.wxy.aicustomer.config.AppProperties;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 切片测试：长文本切多片并补 chunkIndex，短文本保持单条。
 */
class ChunkServiceTest {

    private final ChunkService chunkService = new ChunkService(properties());

    @Test
    void shouldSplitLongTextAndKeepMetadata() {
        String text = ("入住须知：访客需要提前一天预约看房。".repeat(60));
        Document document = Document.builder()
                .text(text)
                .metadata(Map.of("documentId", "doc-1", "fileName", "入住须知.md"))
                .build();

        List<Document> chunks = chunkService.split(List.of(document));

        assertThat(chunks).hasSizeGreaterThan(1);
        for (int index = 0; index < chunks.size(); index++) {
            assertThat(chunks.get(index).getMetadata())
                    .containsEntry("documentId", "doc-1")
                    .containsEntry("fileName", "入住须知.md")
                    .containsEntry("chunkIndex", index);
            assertThat(chunks.get(index).getText()).isNotBlank();
        }
    }

    @Test
    void shouldKeepShortTextAsSingleChunk() {
        Document document = Document.builder()
                .text("退租需要提前 30 天提交申请。")
                .metadata(Map.of("documentId", "doc-2"))
                .build();

        List<Document> chunks = chunkService.split(List.of(document));

        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0).getMetadata()).containsEntry("chunkIndex", 0);
    }

    private AppProperties properties() {
        AppProperties properties = new AppProperties();
        properties.getKnowledge().setChunkSize(50);
        properties.getKnowledge().setMinChunkSizeChars(10);
        properties.getKnowledge().setMinChunkLengthToEmbed(5);
        properties.getKnowledge().setMaxNumChunks(200);
        return properties;
    }
}
