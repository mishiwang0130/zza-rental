package com.wxy.aicustomer.rag;

import com.wxy.aicustomer.config.AppProperties;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 知识库检索测试，重点是"没有向量库/检索失败时降级"的行为。
 */
class RagServiceTest {

    @SuppressWarnings("unchecked")
    private final ObjectProvider<VectorStore> noVectorStore = mock(ObjectProvider.class);

    @Test
    void shouldDegradeWhenVectorStoreMissing() {
        when(noVectorStore.getIfAvailable()).thenReturn(null);

        RetrievalResult result = new RagService(noVectorStore, new AppProperties()).retrieve("退租流程是什么？");

        assertThat(result.hasContext()).isFalse();
        assertThat(result.degraded()).isTrue();
    }

    @Test
    void shouldDegradeWhenSearchThrows() {
        VectorStore vectorStore = mock(VectorStore.class);
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenThrow(new IllegalStateException("qdrant unavailable"));
        ObjectProvider<VectorStore> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(vectorStore);

        RetrievalResult result = new RagService(provider, new AppProperties()).retrieve("退租流程是什么？");

        assertThat(result.degraded()).isTrue();
        assertThat(result.sources()).isEmpty();
    }

    @Test
    void shouldThrowWhenFailFastEnabled() {
        VectorStore vectorStore = mock(VectorStore.class);
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenThrow(new IllegalStateException("boom"));
        ObjectProvider<VectorStore> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(vectorStore);
        AppProperties properties = new AppProperties();
        properties.getRag().setFailFast(true);

        RagService ragService = new RagService(provider, properties);

        assertThatThrownBy(() -> ragService.retrieve("退租流程是什么？"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldMapHitDocumentsToSources() {
        VectorStore vectorStore = mock(VectorStore.class);
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(Document.builder()
                .text("退租需要提前 30 天提交申请，押金在退租后 7 个工作日内退还。")
                .metadata(Map.of("documentId", "doc-9", "fileName", "退租说明.md", "category", "rule"))
                .score(0.87)
                .build()));
        ObjectProvider<VectorStore> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(vectorStore);

        RetrievalResult result = new RagService(provider, new AppProperties()).retrieve("退租要提前多久？");

        assertThat(result.hasContext()).isTrue();
        assertThat(result.sources()).hasSize(1);
        assertThat(result.sources().get(0).fileName()).isEqualTo("退租说明.md");
        assertThat(result.sources().get(0).score()).isEqualTo(0.87);
        assertThat(result.contextText()).contains("退租需要提前 30 天");
    }

    @Test
    void shouldReturnQuestionWhenNoContext() {
        RagService ragService = new RagService(noVectorStore, new AppProperties());

        assertThat(ragService.buildUserPrompt("有哪些空房？", RetrievalResult.empty(false)))
                .isEqualTo("有哪些空房？");
    }

    @Test
    void shouldAppendContextToUserPrompt() {
        RagService ragService = new RagService(noVectorStore, new AppProperties());
        RetrievalResult result = new RetrievalResult(
                List.of(new SourceRef("doc-9", "退租说明.md", "rule", 0.9, "摘要")), "退租需要提前 30 天", false);

        String prompt = ragService.buildUserPrompt("退租要提前多久？", result);

        assertThat(prompt).contains("退租要提前多久？").contains("退租需要提前 30 天");
    }
}
