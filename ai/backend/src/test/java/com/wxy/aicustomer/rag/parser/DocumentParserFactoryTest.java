package com.wxy.aicustomer.rag.parser;

import com.wxy.zzarental.common.exception.ZZAException;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 解析器选择与文本清洗测试。
 */
class DocumentParserFactoryTest {

    private final DocumentParserFactory factory = new DocumentParserFactory(
            List.of(new TextDocumentParser(), new PdfDocumentParser(), new TikaDocumentParser()));

    @Test
    void shouldParseMarkdownAndAttachMetadata() {
        List<Document> documents = factory.parse("入住须知.md", "text/markdown",
                resource("# 入住须知\n\n\n\n访客需提前一天预约。"),
                Map.of("documentId", "doc-1", "fileName", "入住须知.md"));

        assertThat(documents).hasSize(1);
        assertThat(documents.get(0).getText()).isEqualTo("# 入住须知\n\n访客需提前一天预约。");
        assertThat(documents.get(0).getMetadata())
                .containsEntry("documentId", "doc-1")
                .containsEntry("fileName", "入住须知.md");
    }

    @Test
    void shouldRejectUnsupportedFileType() {
        assertThatThrownBy(() -> factory.parse("data.xyz", "application/x-unknown", resource("内容"), Map.of()))
                .isInstanceOf(ZZAException.class)
                .hasMessageContaining("暂不支持的文件类型");
    }

    @Test
    void shouldRejectEmptyContent() {
        assertThatThrownBy(() -> factory.parse("empty.txt", "text/plain", resource("   \n\n "), Map.of()))
                .isInstanceOf(ZZAException.class)
                .hasMessageContaining("解析结果为空");
    }

    private Resource resource(String content) {
        return new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8));
    }
}
