package com.wxy.aicustomer.rag.parser;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Office 文档解析（DOCX / DOC / PPTX / XLSX / HTML），底层使用 Apache Tika。
 */
@Component
public class TikaDocumentParser implements DocumentParser {

    private static final Set<String> SUPPORTED_EXTENSIONS =
            Set.of(".docx", ".doc", ".pptx", ".ppt", ".xlsx", ".xls", ".html", ".htm", ".rtf");

    @Override
    public boolean supports(String fileName, String contentType) {
        String name = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
        if (SUPPORTED_EXTENSIONS.stream().anyMatch(name::endsWith)) {
            return true;
        }
        String type = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
        return type.contains("officedocument") || type.contains("msword") || type.contains("html");
    }

    @Override
    public List<Document> parse(Resource resource, Map<String, Object> metadata) {
        List<Document> documents = new TikaDocumentReader(resource).read();
        return ParserSupport.withMetadata(documents, metadata);
    }
}
