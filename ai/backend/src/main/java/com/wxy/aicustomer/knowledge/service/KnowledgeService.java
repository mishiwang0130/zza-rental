package com.wxy.aicustomer.knowledge.service;

import com.wxy.aicustomer.knowledge.dto.DocumentVo;
import com.wxy.aicustomer.knowledge.dto.KnowledgeSearchRequest;
import com.wxy.aicustomer.knowledge.dto.KnowledgeSearchResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识库管理能力。当前只提供后端接口，通过 Swagger 调试。
 */
public interface KnowledgeService {

    DocumentVo upload(MultipartFile file, String category, String city);

    List<DocumentVo> list();

    DocumentVo rebuild(String documentId);

    void delete(String documentId);

    List<KnowledgeSearchResult> search(KnowledgeSearchRequest request);
}
