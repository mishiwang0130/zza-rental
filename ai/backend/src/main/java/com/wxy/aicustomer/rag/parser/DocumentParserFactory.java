package com.wxy.aicustomer.rag.parser;

import com.wxy.zzarental.common.exception.ZZAException;
import com.wxy.zzarental.common.result.ResultCodeEnum;
import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 按文件名 / Content-Type 选择解析器。
 */
@Component
public class DocumentParserFactory {

    private final List<DocumentParser> parsers;

    public DocumentParserFactory(List<DocumentParser> parsers) {
        this.parsers = parsers;
    }

    public List<Document> parse(String fileName, String contentType, Resource resource, Map<String, Object> metadata) {
        DocumentParser parser = parsers.stream()
                .filter(candidate -> candidate.supports(fileName, contentType))
                .findFirst()
                .orElseThrow(() -> new ZZAException(ResultCodeEnum.PARAM_ERROR.getCode(),
                        "暂不支持的文件类型：" + fileName));
        List<Document> documents = parser.parse(resource, metadata);
        if (documents.isEmpty()) {
            throw new ZZAException(ResultCodeEnum.PARAM_ERROR.getCode(),
                    "文档解析结果为空，请检查文件内容：" + fileName);
        }
        return documents;
    }

    public List<String> supportedExtensions() {
        return List.of(".md", ".markdown", ".txt", ".pdf", ".docx", ".doc", ".pptx", ".xlsx", ".html", ".rtf");
    }
}
