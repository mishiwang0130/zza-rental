package com.wxy.aicustomer.knowledge.storage;

import com.wxy.aicustomer.config.AppProperties;
import com.wxy.zzarental.common.minio.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * MinIO 对象存储实现，架构设计中的可选方案，开启方式：app.storage.type=minio。
 *
 * <p>MinioClient 与连接配置全部复用 common：minio.endpoint / minio.access-key /
 * minio.secret-key / minio.bucket-name，本服务只负责对象 key 的组织。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.storage.type", havingValue = "minio")
public class MinioFileStorageService implements FileStorageService {

    private final MinioClient minioClient;
    private final String bucket;
    private final String prefix;

    public MinioFileStorageService(MinioClient minioClient,
                                   MinioProperties minioProperties,
                                   AppProperties properties) {
        this.minioClient = minioClient;
        this.bucket = minioProperties.getBucketName();
        this.prefix = properties.getStorage().getMinio().getPrefix();
        ensureBucket();
    }

    @Override
    public String store(String documentId, String fileName, InputStream content) {
        String objectName = prefix + documentId + "/" + sanitize(fileName);
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(content, -1, 10 * 1024 * 1024)
                    .build());
        } catch (Exception e) {
            throw new IllegalStateException("MinIO 上传失败：" + objectName, e);
        }
        return objectName;
    }

    @Override
    public Resource load(String storageKey) {
        try {
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(storageKey)
                    .build());
            return new InputStreamResource(stream);
        } catch (Exception e) {
            throw new IllegalStateException("MinIO 读取失败：" + storageKey, e);
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(storageKey)
                    .build());
        } catch (Exception e) {
            log.warn("MinIO 删除失败：{}", storageKey, e);
        }
    }

    private void ensureBucket() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("已创建 MinIO Bucket {}", bucket);
            }
        } catch (Exception e) {
            log.warn("MinIO Bucket 检查失败，请确认 MinIO 可用：{}", e.getMessage());
        }
    }

    private String sanitize(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "document";
        }
        return fileName.replaceAll("[\\\\/:*?\"<>|\\s]", "_");
    }
}
