package com.wxy.aicustomer.rag.parser;

import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Markdown / 纯文本解析，无需额外依赖。
 */
@Component
public class TextDocumentParser implements DocumentParser {

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(".md", ".markdown", ".txt", ".text");

    @Override
    public boolean supports(String fileName, String contentType) {
        if (StringUtils.hasText(contentType)) {
            String type = contentType.toLowerCase(Locale.ROOT);
            if (type.startsWith("text/") || type.contains("markdown")) {
                return true;
            }
        }
        String name = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
        return SUPPORTED_EXTENSIONS.stream().anyMatch(name::endsWith);
    }

    @Override
    public List<Document> parse(Resource resource, Map<String, Object> metadata) {
        String text;
        try (InputStream inputStream = resource.getInputStream()) {
            text = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("文本文件读取失败：" + resource.getFilename(), e);
        }
        String cleaned = ParserSupport.clean(text);
        if (!StringUtils.hasText(cleaned)) {
            return List.of();
        }
        return ParserSupport.withMetadata(List.of(new Document(cleaned)), metadata);
    }
}
