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
 * 文本切片：按 token 切分并补 chunkIndex，供向量化使用。
 */
@Service
@RequiredArgsConstructor
public class ChunkService {

    private static final String META_CHUNK_INDEX = "chunkIndex";

    private final AppProperties properties;

    public List<Document> split(List<Document> documents) {
        TokenTextSplitter splitter = buildSplitter();
        List<Document> chunks = splitter.apply(documents);
        List<Document> result = new ArrayList<>(chunks.size());
        for (int index = 0; index < chunks.size(); index++) {
            Document chunk = chunks.get(index);
            Map<String, Object> metadata = new HashMap<>(chunk.getMetadata());
            metadata.put(META_CHUNK_INDEX, index);
            result.add(Document.builder()
                    .text(chunk.getText())
                    .metadata(metadata)
                    .build());
        }
        return result;
    }

    private TokenTextSplitter buildSplitter() {
        AppProperties.Knowledge knowledge = properties.getKnowledge();
        return TokenTextSplitter.builder()
                .withChunkSize(knowledge.getChunkSize())
                .withMinChunkSizeChars(knowledge.getMinChunkSizeChars())
                .withMinChunkLengthToEmbed(knowledge.getMinChunkLengthToEmbed())
                .withMaxNumChunks(knowledge.getMaxNumChunks())
                .withKeepSeparator(knowledge.isKeepSeparator())
                .build();
    }
}
