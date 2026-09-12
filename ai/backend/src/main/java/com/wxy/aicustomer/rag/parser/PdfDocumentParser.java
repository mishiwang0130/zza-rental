package com.wxy.aicustomer.rag.parser;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * PDF 解析，按页生成 Document，保留页码 Metadata。
 */
@Component
public class PdfDocumentParser implements DocumentParser {

    @Override
    public boolean supports(String fileName, String contentType) {
        if (StringUtils.hasText(contentType) && contentType.toLowerCase(Locale.ROOT).contains("pdf")) {
            return true;
        }
        return fileName != null && fileName.toLowerCase(Locale.ROOT).endsWith(".pdf");
    }

    @Override
    public List<Document> parse(Resource resource, Map<String, Object> metadata) {
        List<Document> documents = new PagePdfDocumentReader(resource).read();
        return ParserSupport.withMetadata(documents, metadata);
    }
}
