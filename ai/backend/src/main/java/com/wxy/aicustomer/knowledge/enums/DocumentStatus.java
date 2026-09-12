package com.wxy.aicustomer.knowledge.enums;

/**
 * 知识文档的状态。
 */
public enum DocumentStatus {

    /** 已保存原始文件，等待或正在向量化 */
    PENDING,

    /** 已完成切片与向量化 */
    INDEXED,

    /** 解析或入库失败 */
    FAILED
}
