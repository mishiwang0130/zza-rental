package com.wxy.aicustomer.rag.parser;

import org.springframework.ai.document.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 解析器的公共工具方法。
 */
final class ParserSupport {

    private ParserSupport() {
    }

    /**
     * 给解析结果补上统一的 Metadata，并把值收敛成 Qdrant 能接受的类型。
     */
    static List<Document> withMetadata(List<Document> documents, Map<String, Object> extraMetadata) {
        List<Document> result = new ArrayList<>(documents.size());
        for (Document document : documents) {
            Map<String, Object> metadata = new HashMap<>();
            document.getMetadata().forEach((key, value) -> putIfSupported(metadata, key, value));
            extraMetadata.forEach((key, value) -> putIfSupported(metadata, key, value));
            result.add(Document.builder()
                    .text(document.getText())
                    .metadata(metadata)
                    .build());
        }
        return result;
    }

    static void putIfSupported(Map<String, Object> metadata, String key, Object value) {
        // Qdrant 的 payload 只接受标量/数组等有限类型：String / Number / Boolean 原样存，
        // 其余（日期对象、嵌套对象等）统一转成字符串，避免写库时报类型不支持
        if (key == null || value == null) {
            return;
        }
        if (value instanceof String || value instanceof Number || value instanceof Boolean) {
            metadata.put(key, value);
        } else {
            metadata.put(key, String.valueOf(value));
        }
    }

    /**
     * 文本清洗：去掉 BOM、统一换行、压缩连续空行。
     *
     * <p>清洗放在解析之后、切片之前：脏字符（尤其 Windows 的 \r\n 和 BOM）会干扰分词，
     * 连续空行则会让切片里混入大量无意义字符、稀释向量语义。
     */
    static String clean(String text) {
        if (text == null) {
            return "";
        }
        String cleaned = text.replace("\uFEFF", "")
                .replace("\r\n", "\n")
                .replace('\r', '\n');
        return cleaned.replaceAll("\n{3,}", "\n\n").trim();
    }
}
