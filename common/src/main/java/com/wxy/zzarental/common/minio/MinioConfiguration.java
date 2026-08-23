package com.wxy.zzarental.common.minio;

import io.minio.MinioClient;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@EnableConfigurationProperties(MinioProperties.class)
@Configuration
public class MinioConfiguration {
    @Resource
    private MinioProperties minioProperties;

    @Bean
    public MinioClient minioClient() {
          return MinioClient.builder().endpoint(minioProperties.getEndpoint())
                  .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                  .build();
    }
}
