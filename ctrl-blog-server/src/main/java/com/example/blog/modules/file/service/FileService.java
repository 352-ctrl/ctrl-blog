package com.example.blog.modules.file.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

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

    /**
     * 清理孤儿文件（删除不在保留列表中的、且创建时间超过24小时的文件）
     * @param activeFileNames 正在使用的活跃文件名集合
     * @return 成功删除的文件数量
     */
    int clearOrphanFiles(Set<String> activeFileNames);
}
