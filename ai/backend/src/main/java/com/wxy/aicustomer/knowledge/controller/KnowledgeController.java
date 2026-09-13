package com.wxy.aicustomer.knowledge.controller;

import com.wxy.aicustomer.knowledge.dto.DocumentVo;
import com.wxy.aicustomer.knowledge.dto.KnowledgeSearchRequest;
import com.wxy.aicustomer.knowledge.dto.KnowledgeSearchResult;
import com.wxy.aicustomer.knowledge.service.KnowledgeService;
import com.wxy.zzarental.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识库接口。当前阶段只做后端，通过 Swagger 上传与调试。
 */
@Tag(name = "知识库", description = "文档上传、删除、重建与语义检索调试")
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @Operation(summary = "上传文档", description = "支持 Markdown / TXT / PDF / DOCX 等，自动解析切片并写入向量库")
    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<DocumentVo> upload(@RequestPart("file") MultipartFile file,
                                     @RequestParam(value = "category", required = false) String category,
                                     @RequestParam(value = "city", required = false) String city) {
        return Result.ok(knowledgeService.upload(file, category, city));
    }

    @Operation(summary = "文档列表", description = "查看已上传文档记录，便于 Swagger 调试")
    @GetMapping("/documents")
    public Result<List<DocumentVo>> list() {
        return Result.ok(knowledgeService.list());
    }

    @Operation(summary = "重建文档索引", description = "重新解析、切片并覆盖向量")
    @PostMapping("/documents/{id}/rebuild")
    public Result<DocumentVo> rebuild(@PathVariable("id") String id) {
        return Result.ok(knowledgeService.rebuild(id));
    }

    @Operation(summary = "删除文档", description = "同时删除原始文件与该文档的全部向量切片")
    @DeleteMapping("/documents/{id}")
    public Result<Void> delete(@PathVariable("id") String id) {
        knowledgeService.delete(id);
        return Result.ok();
    }

    @Operation(summary = "语义检索调试", description = "直接返回 TopK 切片，便于确认知识库效果")
    @PostMapping("/search")
    public Result<List<KnowledgeSearchResult>> search(@Valid @RequestBody KnowledgeSearchRequest request) {
        return Result.ok(knowledgeService.search(request));
    }
}
