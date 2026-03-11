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
    String upload(MultipartFile file, String dir);

    /**
     * 上传文件 (内部字节流专用)
     * <p>通常用于服务器端动态生成的图片（如 Playwright 截图、AI 生成图片）直接上传。
     * 全程在内存中完成，避免了生成本地临时文件带来的磁盘 I/O 损耗与垃圾残留。</p>
     *
     * @param fileData         文件的字节数组数据 (内存流)
     * @param originalFilename 原始文件名（用于提取后缀名，如 "cover.png"）
     * @return 文件访问 URL
     */
    String upload(byte[] fileData, String originalFilename, String dir);

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
