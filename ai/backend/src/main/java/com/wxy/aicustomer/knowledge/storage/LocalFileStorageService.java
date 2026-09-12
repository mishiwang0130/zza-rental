package com.wxy.aicustomer.knowledge.storage;

import com.wxy.aicustomer.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 本地磁盘存储，默认实现。目录结构：{localPath}/{documentId}/{fileName}
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {

    private final Path root;

    public LocalFileStorageService(AppProperties properties) {
        this.root = Paths.get(properties.getStorage().getLocalPath()).toAbsolutePath().normalize();
    }

    @Override
    public String store(String documentId, String fileName, InputStream content) {
        String safeName = sanitize(fileName);
        Path target = resolve(documentId + "/" + safeName);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("原始文件保存失败：" + target, e);
        }
        return documentId + "/" + safeName;
    }

    @Override
    public Resource load(String storageKey) {
        Path target = resolve(storageKey);
        if (!Files.exists(target)) {
            throw new IllegalStateException("原始文件不存在：" + storageKey);
        }
        return new FileSystemResource(target);
    }

    @Override
    public void delete(String storageKey) {
        try {
            Path target = resolve(storageKey);
            Files.deleteIfExists(target);
            Path parent = target.getParent();
            if (parent != null && Files.isDirectory(parent)) {
                try (var children = Files.list(parent)) {
                    if (children.findAny().isEmpty()) {
                        Files.deleteIfExists(parent);
                    }
                }
            }
        } catch (IOException e) {
            log.warn("原始文件删除失败：{}", storageKey, e);
        }
    }

    private Path resolve(String relative) {
        Path target = root.resolve(relative).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("非法的存储路径：" + relative);
        }
        return target;
    }

    private String sanitize(String fileName) {
        String name = fileName == null ? "document" : fileName.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1);
        return name.isBlank() ? "document" : name;
    }
}
