-- 测试用的表结构（H2 内存库，MODE=MySQL）。
-- 字段与 MySQL 中真实的 ai_knowledge_document 表保持一致，
-- 差异只在 H2 不支持的表注释、ON UPDATE CURRENT_TIMESTAMP 等写法上。

CREATE TABLE IF NOT EXISTS ai_knowledge_document (
    id            VARCHAR(64)  NOT NULL,
    file_name     VARCHAR(255) NOT NULL,
    content_type  VARCHAR(128) DEFAULT NULL,
    category      VARCHAR(64)  NOT NULL DEFAULT 'default',
    city          VARCHAR(32)  NOT NULL DEFAULT '通用',
    file_size     BIGINT       NOT NULL DEFAULT 0,
    storage_key   VARCHAR(512) DEFAULT NULL,
    chunk_count   INT          NOT NULL DEFAULT 0,
    status        VARCHAR(32)  NOT NULL DEFAULT 'PENDING',
    error_message VARCHAR(512) DEFAULT NULL,
    create_time   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_delete     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);
