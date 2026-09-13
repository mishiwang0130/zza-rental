package com.wxy.aicustomer.knowledge.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wxy.aicustomer.knowledge.enums.DocumentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 知识库文档记录，对应 MySQL 表 ai_knowledge_document。
 *
 * <p>只描述"上传了什么文档、属于哪个城市、切片多少、是否入库"，
 * 正文存在向量库与原始文件里。
 *
 * <p>主键用 UUID 字符串（服务端生成）：它同时是向量库里的 documentId、
 * 原始文件的存储路径前缀，用同一个值可以避免多套 ID 互相映射。
 * 审计字段沿用 ai_chat / ai_chat_message 的风格：create_time / update_time / is_delete，
 * 由数据库默认值维护，代码只读。
 */
@Schema(description = "知识库文档记录")
@TableName("ai_knowledge_document")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeDocument {

    @Schema(description = "文档 ID（UUID），同时作为向量库 metadata 里的 documentId")
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @Schema(description = "原始文件名")
    @TableField("file_name")
    private String fileName;

    @Schema(description = "文件 MIME 类型")
    @TableField("content_type")
    private String contentType;

    @Schema(description = "知识分类，例如「租赁规定」「费用说明」")
    @TableField("category")
    private String category;

    @Schema(description = "适用城市标签；平台级通用文档为「通用」")
    @TableField("city")
    private String city;

    @Schema(description = "文件大小（字节）")
    @TableField("file_size")
    private long fileSize;

    @Schema(description = "原始文件存储位置（本地相对路径或 MinIO 对象名），用于重建索引")
    @TableField("storage_key")
    private String storageKey;

    @Schema(description = "切片数量")
    @TableField("chunk_count")
    private int chunkCount;

    @Schema(description = "状态：PENDING / INDEXED / FAILED")
    @TableField("status")
    private DocumentStatus status;

    @Schema(description = "失败原因")
    // ALWAYS：重建成功时需要把旧的失败原因更新成 null
    @TableField(value = "error_message", updateStrategy = FieldStrategy.ALWAYS)
    private String errorMessage;

    /** create_time / update_time 交给数据库默认值维护 */
    @Schema(description = "创建时间")
    @TableField(value = "create_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(value = "update_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;

    @Schema(description = "逻辑删除：0 未删除，1 已删除")
    @TableField("is_delete")
    @TableLogic
    private Integer isDelete;
}
