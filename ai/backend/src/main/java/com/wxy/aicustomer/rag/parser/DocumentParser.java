package com.wxy.aicustomer.rag.parser;

import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;

/**
 * 文档解析扩展点：新增文件类型时实现该接口并注册为 Spring Bean 即可。
 */
public interface DocumentParser {

    boolean supports(String fileName, String contentType);

    List<Document> parse(Resource resource, Map<String, Object> metadata);
}
