package com.wxy.zzarental.web.admin.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    /**
     * 上传
     *
     * @param file 文件
     * @return {@code String }
     * @author wxy
     * @date 2026/08/23
     */
    String upload(MultipartFile file);
}
