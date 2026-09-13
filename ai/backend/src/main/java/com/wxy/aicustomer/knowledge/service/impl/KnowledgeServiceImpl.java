package com.wxy.aicustomer.knowledge.service.impl;

import com.wxy.aicustomer.config.AppProperties;
import com.wxy.aicustomer.knowledge.dto.DocumentVo;
import com.wxy.aicustomer.knowledge.dto.KnowledgeSearchRequest;
import com.wxy.aicustomer.knowledge.dto.KnowledgeSearchResult;
import com.wxy.aicustomer.knowledge.entity.KnowledgeDocument;
import com.wxy.aicustomer.knowledge.enums.DocumentStatus;
import com.wxy.aicustomer.knowledge.repository.KnowledgeDocumentRepository;
import com.wxy.aicustomer.knowledge.service.KnowledgeService;
import com.wxy.aicustomer.knowledge.storage.FileStorageService;
import com.wxy.aicustomer.rag.ChunkService;
import com.wxy.aicustomer.rag.CityFilterExpression;
import com.wxy.aicustomer.rag.KnowledgeMetadataKeys;
import com.wxy.aicustomer.rag.parser.DocumentParserFactory;
import com.wxy.zzarental.common.exception.ZZAException;
import com.wxy.zzarental.common.result.ResultCodeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 知识库管理：上传 → 存原文件 → 解析 → 切片 → 向量化；删除与重建同样走这一条链路。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {

    private static final String DEFAULT_CATEGORY = "default";

    private final KnowledgeDocumentRepository documentRepository;
    private final FileStorageService fileStorageService;
    private final DocumentParserFactory documentParserFactory;
    private final ChunkService chunkService;
    private final ObjectProvider<VectorStore> vectorStoreProvider;
    private final AppProperties properties;

    @Override
    public DocumentVo upload(MultipartFile file, String category, String city) {
        if (file == null || file.isEmpty()) {
            throw new ZZAException(ResultCodeEnum.PARAM_ERROR.getCode(), "上传文件不能为空");
        }
        long maxSize = properties.getKnowledge().getMaxFileSizeMb() * 1024L * 1024L;
        if (file.getSize() > maxSize) {
            throw new ZZAException(ResultCodeEnum.PARAM_ERROR.getCode(),
                    "文件超过大小限制：" + properties.getKnowledge().getMaxFileSizeMb() + "MB");
        }
        String fileName = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "document";
        String documentId = UUID.randomUUID().toString();
        String storageKey;
        try (InputStream inputStream = file.getInputStream()) {
            storageKey = fileStorageService.store(documentId, fileName, inputStream);
        } catch (IOException e) {
            throw new ZZAException(ResultCodeEnum.PARAM_ERROR.getCode(), "读取上传文件失败：" + e.getMessage());
        }

        KnowledgeDocument document = KnowledgeDocument.builder()
                .id(documentId)
                .fileName(fileName)
                .contentType(file.getContentType())
                .category(StringUtils.hasText(category) ? category.trim() : DEFAULT_CATEGORY)
                .city(resolveCity(city))
                .size(file.getSize())
                .storageKey(storageKey)
                .status(DocumentStatus.PENDING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        documentRepository.save(document);
        log.info("已接收知识文档 {}（{}），开始向量化", fileName, documentId);
        return index(document);
    }

    @Override
    public List<DocumentVo> list() {
        return documentRepository.findAll().stream().map(this::toVo).toList();
    }

    @Override
    public DocumentVo rebuild(String documentId) {
        KnowledgeDocument document = requireDocument(documentId);
        deleteVectors(documentId);
        document.setStatus(DocumentStatus.PENDING);
        documentRepository.save(document);
        log.info("重建知识文档 {}", documentId);
        return index(document);
    }

    @Override
    public void delete(String documentId) {
        KnowledgeDocument document = requireDocument(documentId);
        deleteVectors(documentId);
        fileStorageService.delete(document.getStorageKey());
        documentRepository.deleteById(documentId);
        log.info("已删除知识文档 {}", documentId);
    }

    @Override
    public List<KnowledgeSearchResult> search(KnowledgeSearchRequest request) {
        AppProperties.Rag rag = properties.getRag();
        int topK = request.topK() == null ? rag.getTopK() : request.topK();
        double threshold = request.similarityThreshold() == null
                ? rag.getSimilarityThreshold()
                : request.similarityThreshold();
        SearchRequest.Builder builder = SearchRequest.builder()
                .query(request.query())
                .topK(topK)
                .similarityThreshold(threshold);
        Filter.Expression cityFilter = CityFilterExpression.build(
                request.city(), rag.getCityMetadataKey(), rag.getCommonCity());
        if (cityFilter != null) {
            builder.filterExpression(cityFilter);
        }
        List<Document> documents = requireVectorStore().similaritySearch(builder.build());
        return documents.stream().map(this::toSearchResult).toList();
    }

    private DocumentVo index(KnowledgeDocument document) {
        try {
            var resource = fileStorageService.load(document.getStorageKey());
            // 历史记录可能没有城市标签，重建时统一落到"通用"，避免写入空值 payload
            String city = StringUtils.hasText(document.getCity())
                    ? document.getCity()
                    : properties.getRag().getCommonCity();
            document.setCity(city);
            Map<String, Object> metadata = new HashMap<>();
            metadata.put(KnowledgeMetadataKeys.DOCUMENT_ID, document.getId());
            metadata.put(KnowledgeMetadataKeys.FILE_NAME, document.getFileName());
            metadata.put(KnowledgeMetadataKeys.CATEGORY, document.getCategory());
            metadata.put(KnowledgeMetadataKeys.CITY, city);

            List<Document> parsed = documentParserFactory.parse(
                    document.getFileName(), document.getContentType(), resource, metadata);
            List<Document> chunks = chunkService.split(parsed);
            requireVectorStore().add(chunks);

            document.setChunkCount(chunks.size());
            document.setStatus(DocumentStatus.INDEXED);
            document.setErrorMessage(null);
            document.setUpdatedAt(Instant.now());
            documentRepository.save(document);
            log.info("知识文档 {} 向量化完成，切片 {} 个", document.getId(), chunks.size());
            return toVo(document);
        } catch (Exception ex) {
            log.error("知识文档 {} 入库失败", document.getId(), ex);
            document.setStatus(DocumentStatus.FAILED);
            document.setErrorMessage(ex.getMessage());
            document.setUpdatedAt(Instant.now());
            documentRepository.save(document);
            throw new ZZAException(ResultCodeEnum.SERVICE_ERROR.getCode(), "文档入库失败：" + ex.getMessage());
        }
    }

    private void deleteVectors(String documentId) {
        VectorStore vectorStore = vectorStoreProvider.getIfAvailable();
        if (vectorStore == null) {
            return;
        }
        try {
            var expression = new FilterExpressionBuilder()
                    .eq(KnowledgeMetadataKeys.DOCUMENT_ID, documentId)
                    .build();
            vectorStore.delete(expression);
        } catch (Exception ex) {
            log.warn("删除文档 {} 的向量失败：{}", documentId, ex.getMessage());
        }
    }

    private KnowledgeDocument requireDocument(String documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new ZZAException(ResultCodeEnum.DATA_ERROR.getCode(),
                        "知识文档不存在：" + documentId));
    }

    private VectorStore requireVectorStore() {
        VectorStore vectorStore = vectorStoreProvider.getIfAvailable();
        if (vectorStore == null) {
            throw new ZZAException(ResultCodeEnum.SERVICE_ERROR.getCode(),
                    "向量库未就绪，请检查 Qdrant 配置与 Embedding 模型");
        }
        return vectorStore;
    }

    /**
     * 上传未指定城市时落到平台级通用标签，保证任何城市检索都能召回该文档。
     */
    private String resolveCity(String city) {
        return StringUtils.hasText(city) ? city.trim() : properties.getRag().getCommonCity();
    }

    private DocumentVo toVo(KnowledgeDocument document) {
        return new DocumentVo(document.getId(), document.getFileName(), document.getCategory(),
                document.getCity(), document.getContentType(), document.getSize(), document.getChunkCount(),
                document.getStatus() == null ? null : document.getStatus().name(),
                document.getErrorMessage(), document.getCreatedAt(), document.getUpdatedAt());
    }

    private KnowledgeSearchResult toSearchResult(Document document) {
        Map<String, Object> metadata = document.getMetadata();
        Object chunkIndex = metadata.get(KnowledgeMetadataKeys.CHUNK_INDEX);
        return new KnowledgeSearchResult(
                asText(metadata.get(KnowledgeMetadataKeys.DOCUMENT_ID)),
                asText(metadata.get(KnowledgeMetadataKeys.FILE_NAME)),
                asText(metadata.get(KnowledgeMetadataKeys.CATEGORY)),
                asText(metadata.get(KnowledgeMetadataKeys.CITY)),
                chunkIndex instanceof Number number ? number.intValue() : null,
                document.getScore(),
                document.getText());
    }

    private String asText(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
