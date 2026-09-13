package com.wxy.aicustomer.rag.parser;

import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;

/**
 * 文档解析扩展点（RAG 入库链路的第 3 步）。
 *
 * <p>为什么抽成接口：入库流程只关心"给我一个 Resource，还我一堆 Document"，
 * 具体是纯文本、PDF 还是 Office 文档，由实现类自己判断。
 * 新增一类文件时的改动是"新加一个实现类 + @Component"，不用改工厂、不用改 Service，
 * 符合开闭原则（DocumentParserFactoryTest 就是按这个约定写的）。
 *
 * <p>两个方法的约定：
 * <ul>
 *   <li>{@code supports}：按文件名后缀或 Content-Type 判断自己能不能处理。
 *       工厂按 Bean 顺序取第一个 supports=true 的实现，所以判断条件要尽量精确；</li>
 *   <li>{@code parse}：把文件读成 Spring AI 的 Document 列表。PDF 按页返回多个 Document，
 *       文本类返回一个；解析完必须把传入的 metadata 带上（用 ParserSupport.withMetadata）。</li>
 * </ul>
 */
public interface DocumentParser {

    boolean supports(String fileName, String contentType);

    List<Document> parse(Resource resource, Map<String, Object> metadata);
}
