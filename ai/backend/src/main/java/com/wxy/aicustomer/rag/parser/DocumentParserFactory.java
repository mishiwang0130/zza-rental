package com.wxy.aicustomer.rag.parser;

import com.wxy.zzarental.common.exception.ZZAException;
import com.wxy.zzarental.common.result.ResultCodeEnum;
import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 解析器工厂：按文件名 / Content-Type 从一组 {@link DocumentParser} 里挑一个来解析。
 *
 * <p><b>这是简历里"策略 + 工厂"说法真正的落点</b>：
 * Spring 启动时把所有 DocumentParser 实现注入成 List（策略集合），
 * 工厂在运行时按 supports() 选具体策略，调用方（KnowledgeServiceImpl）不感知文件类型。
 *
 * <p>当前注册的三种策略：
 * <ul>
 *   <li>TextDocumentParser：.md / .txt 等，直接按 UTF-8 读文本；</li>
 *   <li>PdfDocumentParser：PDF，底层 PagePdfDocumentReader，按页生成 Document 并保留页码；</li>
 *   <li>TikaDocumentParser：docx / doc / pptx / xlsx / html / rtf，底层 Apache Tika。</li>
 * </ul>
 */
@Component
public class DocumentParserFactory {

    private final List<DocumentParser> parsers;

    public DocumentParserFactory(List<DocumentParser> parsers) {
        this.parsers = parsers;
    }

    public List<Document> parse(String fileName, String contentType, Resource resource, Map<String, Object> metadata) {
        // 找不到能处理的解析器就报参数错误（对用户是 202 参数错误，不是 500）
        DocumentParser parser = parsers.stream()
                .filter(candidate -> candidate.supports(fileName, contentType))
                .findFirst()
                .orElseThrow(() -> new ZZAException(ResultCodeEnum.PARAM_ERROR.getCode(),
                        "暂不支持的文件类型：" + fileName));
        List<Document> documents = parser.parse(resource, metadata);
        // 解析出空结果是常见故障（扫描版 PDF、只有图片的 docx），这里明确报错，
        // 避免"上传成功但知识库里什么都没有"这种难排查的状态
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
