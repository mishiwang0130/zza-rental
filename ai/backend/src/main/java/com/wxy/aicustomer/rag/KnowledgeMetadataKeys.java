package com.wxy.aicustomer.rag;

/**
 * 知识切片写入向量库时使用的 metadata 键名。
 *
 * <p>上传侧（写 payload）与检索侧（拼过滤条件）共用同一份常量，避免两边拼写漂移。
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
