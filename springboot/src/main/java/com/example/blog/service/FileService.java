package com.example.blog.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    /**
     * 上传文件
     * @param file 文件对象
     * @return 文件访问 URL
     */
    String upload(MultipartFile file);

    /**
     * 下载文件
     * @param fileName 文件名
     * @param response 响应对象
     */
    void download(String fileName, HttpServletResponse response);
}
