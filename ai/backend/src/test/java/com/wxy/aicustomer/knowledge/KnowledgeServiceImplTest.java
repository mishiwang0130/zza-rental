package com.wxy.aicustomer.knowledge;

import com.wxy.aicustomer.config.AppProperties;
import com.wxy.aicustomer.knowledge.dto.DocumentVo;
import com.wxy.aicustomer.knowledge.entity.KnowledgeDocument;
import com.wxy.aicustomer.knowledge.enums.DocumentStatus;
import com.wxy.aicustomer.knowledge.repository.KnowledgeDocumentRepository;
import com.wxy.aicustomer.knowledge.service.impl.KnowledgeServiceImpl;
import com.wxy.aicustomer.knowledge.storage.FileStorageService;
import com.wxy.aicustomer.rag.ChunkService;
import com.wxy.aicustomer.rag.KnowledgeMetadataKeys;
import com.wxy.aicustomer.rag.parser.DocumentParserFactory;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 知识文档上传测试：城市标签写入切片 metadata，未指定城市时落到"通用"。
 */
class KnowledgeServiceImplTest {

    @Test
    void shouldWriteCityMetadataWhenCityProvided() {
        Fixture fixture = fixture();

        DocumentVo vo = fixture.service.upload(uploadFile(), "租赁规定", "武汉");

        assertThat(vo.city()).isEqualTo("武汉");
        assertThat(vo.category()).isEqualTo("租赁规定");
        assertThat(capturedChunkMetadata(fixture)).containsEntry(KnowledgeMetadataKeys.CITY, "武汉");
        assertThat(capturedDocument(fixture).getCity()).isEqualTo("武汉");
    }

    @Test
    void shouldDefaultToCommonCityWhenCityMissing() {
        Fixture fixture = fixture();

        DocumentVo vo = fixture.service.upload(uploadFile(), null, "  ");

        assertThat(vo.city()).isEqualTo("通用");
        assertThat(vo.category()).isEqualTo("default");
        assertThat(capturedChunkMetadata(fixture)).containsEntry(KnowledgeMetadataKeys.CITY, "通用");
    }

    @Test
    void shouldRepairLegacyDocumentCityOnRebuild() {
        Fixture fixture = fixture();
        KnowledgeDocument legacy = KnowledgeDocument.builder()
                .id("legacy-1")
                .fileName("历史文档.docx")
                .contentType("application/docx")
                .category("租赁规定")
                .storageKey("key-1")
                .status(DocumentStatus.INDEXED)
                .build();
        when(fixture.documentRepository.findById("legacy-1")).thenReturn(Optional.of(legacy));

        DocumentVo vo = fixture.service.rebuild("legacy-1");

        assertThat(vo.city()).isEqualTo("通用");
        assertThat(capturedChunkMetadata(fixture)).containsEntry(KnowledgeMetadataKeys.CITY, "通用");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> capturedChunkMetadata(Fixture fixture) {
        ArgumentCaptor<List<Document>> captor = ArgumentCaptor.forClass(List.class);
        verify(fixture.vectorStore).add(captor.capture());
        return captor.getValue().get(0).getMetadata();
    }

    private KnowledgeDocument capturedDocument(Fixture fixture) {
        ArgumentCaptor<KnowledgeDocument> captor = ArgumentCaptor.forClass(KnowledgeDocument.class);
        // 上传先存 PENDING 记录，索引完成后再存一次 INDEXED，两次都应带上城市标签
        verify(fixture.documentRepository, times(2)).save(captor.capture());
        List<KnowledgeDocument> saved = captor.getAllValues();
        assertThat(saved).allSatisfy(document -> assertThat(document.getCity()).isNotBlank());
        return saved.get(saved.size() - 1);
    }

    private MockMultipartFile uploadFile() {
        return new MockMultipartFile("file", "武汉_公寓租房管理规定.docx", "application/docx",
                "武汉押金为 1 个月租金。".getBytes(StandardCharsets.UTF_8));
    }

    @SuppressWarnings("unchecked")
    private Fixture fixture() {
        KnowledgeDocumentRepository documentRepository = mock(KnowledgeDocumentRepository.class);
        FileStorageService fileStorageService = mock(FileStorageService.class);
        DocumentParserFactory parserFactory = mock(DocumentParserFactory.class);
        VectorStore vectorStore = mock(VectorStore.class);
        ObjectProvider<VectorStore> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(vectorStore);
        when(fileStorageService.store(anyString(), anyString(), any())).thenReturn("key-1");
        Resource resource = new ByteArrayResource("武汉押金为 1 个月租金。".getBytes(StandardCharsets.UTF_8));
        when(fileStorageService.load("key-1")).thenReturn(resource);
        // 解析器只负责把上传时拼好的 metadata 带到 Document 上，这里直接透传第 4 个入参
        when(parserFactory.parse(anyString(), any(), any(), anyMap()))
                .thenAnswer(invocation -> List.of(Document.builder()
                        .text("武汉押金为 1 个月租金。")
                        .metadata(invocation.getArgument(3))
                        .build()));

        KnowledgeServiceImpl service = new KnowledgeServiceImpl(documentRepository, fileStorageService,
                parserFactory, new ChunkService(new AppProperties()), provider, new AppProperties());
        return new Fixture(service, documentRepository, vectorStore);
    }

    private record Fixture(KnowledgeServiceImpl service, KnowledgeDocumentRepository documentRepository,
                           VectorStore vectorStore) {
    }
}
