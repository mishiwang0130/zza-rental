package com.wxy.aicustomer.rag;

/**
 * 知识切片写入向量库时使用的 metadata 键名。
 *
 * <p>Metadata 在 RAG 里的作用：向量库里存的每条数据 = 向量 + 原文 + payload(metadata)。
 * 向量负责"语义像不像"，payload 负责"这条数据属于谁、能不能被召回"。
 * <ul>
 *   <li>写入侧：KnowledgeServiceImpl 在切片前把这些键塞进 Document.metadata，
 *       最终随向量一起存进 Qdrant 的 payload；</li>
 *   <li>检索侧：RagService / CityFilterExpression 按 city 拼过滤条件下推到 Qdrant，
 *       命中后按 documentId / fileName 组装"参考资料"返回给前端；</li>
 *   <li>删除侧：KnowledgeServiceImpl.deleteVectors 按 documentId 精确删除该文档的所有切片。</li>
 * </ul>
 *
 * <p>上传侧（写 payload）与检索侧（拼过滤条件）共用同一份常量，避免两边拼写漂移——
 * 这类键名一旦写错，症状是"检索永远为空"且不报错，很难排查。
 */
public final class KnowledgeMetadataKeys {

    /** 文档 ID，删除与重建索引时按它过滤 */
    public static final String DOCUMENT_ID = "documentId";

    /** 原始文件名，回答引用来源时展示 */
    public static final String FILE_NAME = "fileName";

    /** 业务类别，例如"租赁规定""费用说明" */
    public static final String CATEGORY = "category";

    /** 适用城市标签，例如"武汉""广州""深圳"；平台级通用文档为"通用" */
    public static final String CITY = "city";

    /** 切片序号，同一文档内从 0 开始 */
    public static final String CHUNK_INDEX = "chunkIndex";

    private KnowledgeMetadataKeys() {
    }
}
