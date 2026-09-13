package com.wxy.aicustomer.rag;

import com.wxy.aicustomer.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * RAG 检索与提示词拼接。
 *
 * <p>向量库或 Embedding 不可用时默认降级为纯模型回答（app.rag.fail-fast=false），
 * 保证本地没有中间件时也能跑通链路。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private static final int SNIPPET_MAX_LENGTH = 120;

    private final ObjectProvider<VectorStore> vectorStoreProvider;
    private final AppProperties properties;

    /**
     * 检索知识库。传入城市时按“选中城市 + 通用”过滤，过滤后无命中且开启兜底时回退为不带过滤的检索；
     * 除开启 fail-fast 外，任何异常都会降级为“无参考资料”。
     */
    public RetrievalResult retrieve(String question, String city) {
        AppProperties.Rag rag = properties.getRag();
        if (!rag.isEnabled() || question == null || question.isBlank()) {
            return RetrievalResult.empty(false);
        }
        VectorStore vectorStore = vectorStoreProvider.getIfAvailable();
        if (vectorStore == null) {
            log.debug("未装配 VectorStore，跳过知识库检索");
            return RetrievalResult.empty(true);
        }
        try {
            Filter.Expression cityFilter = CityFilterExpression.build(
                    city, rag.getCityMetadataKey(), rag.getCommonCity());
            List<Document> documents = search(vectorStore, question, rag, cityFilter);
            if ((documents == null || documents.isEmpty())
                    && cityFilter != null && rag.isFallbackToUnfilteredWhenEmpty()) {
                log.debug("城市 {} 过滤后没有命中，回退为不带过滤的检索", city);
                documents = search(vectorStore, question, rag, null);
            }
            if (documents == null || documents.isEmpty()) {
                return RetrievalResult.empty(false);
            }
            return toRetrievalResult(documents, rag.getMaxContextChars());
        } catch (Exception ex) {
            if (rag.isFailFast()) {
                throw ex;
            }
            log.warn("知识库检索失败，本轮降级为纯模型回答：{}", ex.getMessage());
            return RetrievalResult.empty(true);
        }
    }

    /**
     * 把检索结果拼进用户消息。没有命中资料时原样返回问题。
     */
    public String buildUserPrompt(String question, String city, RetrievalResult retrieval) {
        String cityContext = StringUtils.hasText(city) ? "当前城市：" + city.trim() + "\n" : "";
        if (retrieval == null || !retrieval.hasContext()) {
            return cityContext.isEmpty() ? question : cityContext + "用户问题：\n" + question;
        }
        return """
                %s用户问题：
                %s

                参考知识库资料（可能不完整，请结合资料回答；引用时说明来源文件名）：
                %s
                """.formatted(cityContext, question, retrieval.contextText());
    }

    private List<Document> search(VectorStore vectorStore, String question,
                                  AppProperties.Rag rag, Filter.Expression filter) {
        SearchRequest.Builder builder = SearchRequest.builder()
                .query(question)
                .topK(rag.getTopK())
                .similarityThreshold(rag.getSimilarityThreshold());
        if (filter != null) {
            builder.filterExpression(filter);
        }
        return vectorStore.similaritySearch(builder.build());
    }

    private RetrievalResult toRetrievalResult(List<Document> documents, int maxContextChars) {
        List<SourceRef> sources = new ArrayList<>(documents.size());
        StringBuilder context = new StringBuilder();
        int index = 1;
        for (Document document : documents) {
            Map<String, Object> metadata = document.getMetadata();
            String fileName = asText(metadata.get(KnowledgeMetadataKeys.FILE_NAME), "未命名文档");
            String section = """
                    [%d] 来源文件：%s
                    %s

                    """.formatted(index++, fileName, document.getText());
            if (context.length() + section.length() > maxContextChars) {
                break;
            }
            context.append(section);
            sources.add(new SourceRef(
                    asText(metadata.get(KnowledgeMetadataKeys.DOCUMENT_ID), null),
                    fileName,
                    asText(metadata.get(KnowledgeMetadataKeys.CATEGORY), null),
                    document.getScore(),
                    snippet(document.getText())));
        }
        return new RetrievalResult(List.copyOf(sources), context.toString().trim(), false);
    }

    private String snippet(String text) {
        if (text == null) {
            return "";
        }
        String flattened = text.replaceAll("\\s+", " ").trim();
        return flattened.length() <= SNIPPET_MAX_LENGTH
                ? flattened
                : flattened.substring(0, SNIPPET_MAX_LENGTH) + "…";
    }

    private String asText(Object value, String defaultValue) {
        return value == null ? defaultValue : String.valueOf(value);
    }
}
