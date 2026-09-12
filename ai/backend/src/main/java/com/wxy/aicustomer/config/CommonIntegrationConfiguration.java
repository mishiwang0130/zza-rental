package com.wxy.aicustomer.config;

import com.wxy.zzarental.common.exception.GlobalExceptionHandler;
import com.wxy.zzarental.common.minio.MinioConfiguration;
import com.wxy.zzarental.common.util.RedisUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 复用 zza-rental 的 common 模块。
 *
 * <p>AI 服务的扫描根包是 com.wxy.aicustomer，common 里的组件不会被自动扫描，
 * 这里显式引入需要复用的能力：
 * RedisUtil（Redis 通用操作封装，勿另行封装）、
 * GlobalExceptionHandler（与公寓系统一致的统一异常返回：HTTP 200 + Result 业务码）。
 */
@Configuration
@Import({RedisUtil.class, GlobalExceptionHandler.class})
public class CommonIntegrationConfiguration {

    /**
     * 使用 MinIO 时复用 common 的 MinioClient（配置前缀 minio.*）。
     */
    @Configuration
    @ConditionalOnProperty(name = "app.storage.type", havingValue = "minio")
    @Import(MinioConfiguration.class)
    public static class MinioStorageIntegration {
    }
}
