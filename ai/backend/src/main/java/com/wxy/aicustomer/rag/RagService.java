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
 * RAG 检索与提示词拼接：把"用户问题"变成"问题 + 参考资料"的完整提示词。
 *
 * <p><b>在整条问答链路里的位置</b>：
 * <pre>
 *   ChatServiceImpl.stream()
 *     ├─ 【本类 retrieve(question, city)】   检索知识库，拿到片段 + 来源
 *     ├─ 【本类 buildUserPrompt(...)】       拼成"当前城市 + 用户问题 + 参考资料"
 *     ├─ ChatClient.stream()               带 Memory 与 Tool 调用模型，流式返回
 *     └─ SSE sources 事件                   把来源回给前端展示
 * </pre>
 *
 * <p><b>检索发生了什么</b>：{@code vectorStore.similaritySearch(request)} 会把问题文本
 * 交给同一个 Embedding 模型算出查询向量，再在 Qdrant 里做近邻检索，
 * 返回带 score 的 Document 列表（score 越高越相似）。
 *
 * <p><b>降级策略</b>：检索只是"增强"，不是刚性依赖。向量库没装配、Embedding 调用失败、
 * Qdrant 连不上时，只要 app.rag.fail-fast=false，就记一条 warn 并返回"无参考资料"，
 * 让这一轮退化成纯模型回答——用户至少能拿到回复，而不是一个 500。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private static final int SNIPPET_MAX_LENGTH = 120;

    private final ObjectProvider<VectorStore> vectorStoreProvider;
    private final AppProperties properties;

    /**
     * 检索知识库。传入城市时按"选中城市 + 通用"过滤；过滤后无命中且开启兜底时，
     * 回退为不带过滤的检索；除开启 fail-fast 外，任何异常都降级为"无参考资料"。
     *
     * @param question 用户本轮问题（原样作为查询文本，不做改写）
     * @param city     前端选中的城市，可为空
     * @return 命中的来源列表 + 拼好的上下文文本；{@code degraded=true} 表示本轮是降级结果
     */
    public RetrievalResult retrieve(String question, String city) {
        AppProperties.Rag rag = properties.getRag();
        // 开关关闭（例如本地零依赖调试）或问题为空：直接当"没有知识库"，不算降级
        if (!rag.isEnabled() || question == null || question.isBlank()) {
            return RetrievalResult.empty(false);
        }
        // 用 ObjectProvider 而不是直接注入：VectorStore 在测试环境可能压根没有这个 Bean。
        // 这种"依赖可以缺席"的写法是这套代码能在无中间件环境下跑起来的关键。
        VectorStore vectorStore = vectorStoreProvider.getIfAvailable();
        if (vectorStore == null) {
            log.debug("未装配 VectorStore，跳过知识库检索");
            return RetrievalResult.empty(true);
        }
        try {
            // 第一步：按城市拼 payload 过滤条件（选中城市 + 通用；没选城市则为 null 不过滤）
            Filter.Expression cityFilter = CityFilterExpression.build(
                    city, rag.getCityMetadataKey(), rag.getCommonCity());
            // 第二步：TopK + 相似度阈值召回，过滤条件下推到向量库
            List<Document> documents = search(vectorStore, question, rag, cityFilter);
            // 第三步：城市过滤后一条都没命中时兜底——很可能是文档城市标签没打对，
            // 宁可放宽范围也别让用户得不到任何参考资料（开关可关，见 fallback-to-unfiltered-when-empty）
            if ((documents == null || documents.isEmpty())
                    && cityFilter != null && rag.isFallbackToUnfilteredWhenEmpty()) {
                log.debug("城市 {} 过滤后没有命中，回退为不带过滤的检索", city);
                documents = search(vectorStore, question, rag, null);
            }
            // 仍然没有命中：正常返回空，不标记降级（这是"知识库里确实没有"的正常情况）
            if (documents == null || documents.isEmpty()) {
                return RetrievalResult.empty(false);
            }
            // 第四步：组装上下文与来源，超过 maxContextChars 的片段会被截断丢弃
            return toRetrievalResult(documents, rag.getMaxContextChars());
        } catch (Exception ex) {
            // fail-fast=true 时把异常抛给上层（用于排查问题）；默认 false，降级为纯模型回答
            if (rag.isFailFast()) {
                throw ex;
            }
            log.warn("知识库检索失败，本轮降级为纯模型回答：{}", ex.getMessage());
            return RetrievalResult.empty(true);
        }
    }

    /**
     * 把检索结果拼进用户消息——这就是 RAG 里的 "A"（Augmented）落地的地方。
     *
     * <p>有参考资料时的结构：当前城市 → 用户问题 → 参考知识库资料（带《来源文件》），
     * 并明确要求模型"引用时说明来源文件名"，这样回答可溯源，前端也能对应展示 sources。
     *
     * <p>没有命中资料时原样返回问题（只多一行城市），保证行为与不启用 RAG 时一致。
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
        // SearchRequest 的两个核心参数：
        //   topK                —— 召回条数，控制"量"（默认 5，太多会稀释上下文、更贵）
        //   similarityThreshold —— 相似度下限，控制"质"（默认 0.5，低于它的片段直接丢弃，不够就只能召回到更少的片段或者空）
        SearchRequest.Builder builder = SearchRequest.builder()
                .query(question)
                .topK(rag.getTopK())
                .similarityThreshold(rag.getSimilarityThreshold());
        if (filter != null) {
            builder.filterExpression(filter);
        }
        return vectorStore.similaritySearch(builder.build());
    }

    /**
     * 把向量库返回的片段整理成两部分：
     * <ul>
     *   <li>contextText：按 [序号] 来源文件 + 正文 的格式拼起来，直接塞进提示词；</li>
     *   <li>sources：结构化来源（文件名、类别、相似度、摘要），供 SSE sources 事件给前端展示。</li>
     * </ul>
     *
     * <p>maxContextChars 是成本与稳定性的双保险：片段全量拼进去可能超出模型上下文窗口，
     * 也会让每轮请求更贵更慢。这里的策略是"放不下就停止追加"（break），不做截断半个片段的处理。
     */
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
