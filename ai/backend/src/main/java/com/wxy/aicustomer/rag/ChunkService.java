package com.wxy.aicustomer.rag;

import com.wxy.aicustomer.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文本切片服务：把「一篇文档」切成「若干片段（chunk）」。
 *
 * <p><b>在整条 RAG 入库链路里的位置</b>：
 * <pre>
 *   KnowledgeController.upload
 *     ├─ FileStorageService.store()        原始文件先落 MinIO / 本地磁盘
 *     ├─ DocumentParserFactory.parse()     按类型选解析器，产出 Document（已带 metadata）
 *     ├─ 【本类 split()】                   切成 chunk，并补 chunkIndex
 *     └─ VectorStore.add(chunks)           Spring AI 调 Embedding 算向量并写入 Qdrant
 * </pre>
 *
 * <p><b>为什么要切片（面试常问）</b>：
 * <ol>
 *   <li>Embedding 模型和对话模型都有输入长度上限，整篇文档塞不进去；</li>
 *   <li>整篇文档只算一个向量，会把多个主题"平均"掉，检索精度明显下降；</li>
 *   <li>检索的粒度就是切片的粒度：片越干净，拼给模型的参考资料越准，也越省 token。</li>
 * </ol>
 *
 * <p><b>TokenTextSplitter 到底怎么切（用的是 Spring AI 1.1 的实现，务必说准）</b>：
 * <ul>
 *   <li>单位是 <b>token</b> 不是字符：底层用 jtokkit（CL100K_BASE 编码）把文本编码成 token 序列，
 *       再按 chunkSize 个 token 取一段，比按字符数切更贴近模型真实开销；</li>
 *   <li>断句尽量对齐标点：取出一段 token 解码成字符串后，如果后面还有剩余 token，
 *       会先找这串文字里<b>最后一个标点</b>；只要该标点位置超过 minChunkSizeChars，
 *       就在标点处截断，多出来的 token 顺延到下一片。目的是别把一句话劈成两半，
 *       同时避免切出极短的碎片；</li>
 *   <li>丢弃过短碎片：长度小于等于 minChunkLengthToEmbed 的片段直接不进结果，
 *       也就不参与向量化，省 Embedding 调用（注意是"丢弃"，不是"合并"）；</li>
 *   <li>上限保护：最多切 maxNumChunks 片，防止超大文件把向量库写爆；</li>
 *   <li><b>没有重叠（overlap）</b>：TokenTextSplitter 没有 overlap 参数，本项目也没有做重叠。
 *       被问到"片段边界语义被切断"时，正确答法是"靠标点对齐缓解，可以再引入 overlap
 *       或父子切片优化"，而不是说已经做了重叠。</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class ChunkService {

    private final AppProperties properties;

    /**
     * 切分入口。入参是解析器产出的 Document（metadata 已包含 documentId / fileName / category / city），
     * 返回切片后的 Document 列表。
     *
     * <p>注意：一个入参 Document 通常会被切成多片；PDF 按页解析时入参本身就是多个 Document，
     * 这里逐个切分后再合并成一个列表返回。
     */
    public List<Document> split(List<Document> documents) {
        TokenTextSplitter splitter = buildSplitter();
        // splitter.apply() 会继承原 metadata，并额外写入 Spring AI 自带的三个键：
        // parent_document_id（指向切片前的原始 Document id）、chunk_index、total_chunks
        List<Document> chunks = splitter.apply(documents);
        List<Document> result = new ArrayList<>(chunks.size());
        for (int index = 0; index < chunks.size(); index++) {
            Document chunk = chunks.get(index);
            Map<String, Object> metadata = new HashMap<>(chunk.getMetadata());
            // 再补一个本项目自己的全局序号：Spring AI 的 chunk_index 是"相对每个入参 Document"
            // 计数的，PDF 按页解析时会从 0 重新开始；这里统一成跨文档递增的序号，方便展示和排查
            metadata.put(KnowledgeMetadataKeys.CHUNK_INDEX, index);
            // 重新 build 一次：保证返回的切片只带我们可控的 text + metadata
            result.add(Document.builder()
                    .text(chunk.getText())
                    .metadata(metadata)
                    .build());
        }
        return result;
    }

    /**
     * 用配置里的参数构造切片器。所有阈值都来自 application.yml 的 app.knowledge.*，
     * 这样调参不需要改代码（ChunkServiceTest 也是直接改配置断言结果的）。
     */
    private TokenTextSplitter buildSplitter() {
        AppProperties.Knowledge knowledge = properties.getKnowledge();
        return TokenTextSplitter.builder()
                .withChunkSize(knowledge.getChunkSize())                          // 每片目标 token 数：800
                .withMinChunkSizeChars(knowledge.getMinChunkSizeChars())          // 允许断句的最小字符位置：350
                .withMinChunkLengthToEmbed(knowledge.getMinChunkLengthToEmbed())  // 短于该长度的碎片丢弃：5
                .withMaxNumChunks(knowledge.getMaxNumChunks())                    // 单篇文档最多切片数：10000
                .withKeepSeparator(knowledge.isKeepSeparator())                   // 保留片段内换行：可读性更好
                .build();
    }
}
