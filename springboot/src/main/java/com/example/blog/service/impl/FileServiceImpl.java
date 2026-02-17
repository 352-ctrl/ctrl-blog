package com.example.blog.service.impl;

import cn.hutool.core.io.FileUtil;
import com.example.blog.common.constants.Constants;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.exception.CustomerException;
import com.example.blog.service.FileService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

    @Value("${file.upload-path}")
    private String basePath;

    @Value("${file.access-path}")
    private String accessPath;

    /**
     * 初始化方法：Spring 容器启动后自动执行
     * 用于确保路径格式正确，并且目录已创建
     */
    @PostConstruct
    public void init() {
        // 1. 处理路径分隔符，确保最后有斜杠
        if (!basePath.endsWith(File.separator)) {
            basePath += File.separator;
        }

        // 2. 启动时就检查并创建目录，如果有权限问题启动时就会报错，便于排查
        if (!FileUtil.isDirectory(basePath)) {
            FileUtil.mkdir(basePath);
            log.info("已初始化文件存储目录: {}", basePath);
        }
    }

    @Override
    public String upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_FILE_IS_EMPTY);
        }

        try {
            // 1. 生成文件名 (防止文件名冲突 & 路径遍历攻击)
            String originalFilename = file.getOriginalFilename();
            // 使用 Hutool 的 mainName 获取主文件名，防 ../../ 攻击
            String safeName = FileUtil.mainName(originalFilename);
            String extName = FileUtil.extName(originalFilename);
            String fileName = System.currentTimeMillis() + "_" + safeName + "." + extName;

            // 2. 写入磁盘 (使用 Hutool 或 Java NIO)
            File dest = new File(basePath + fileName);
            file.transferTo(dest);

            // 3. 生成 URL
            return accessPath + fileName;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new CustomerException(ResultCode.INTERNAL_SERVER_ERROR, MessageConstants.MSG_UPLOAD_FAILURE);
        }
    }

    @Override
    public void download(String fileName, HttpServletResponse response) {
        // 安全检查：防止下载 files 目录以外的文件 (如 ../application.yml)
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new CustomerException(ResultCode.PARAM_ERROR, "非法的文件名");
        }

        File file = new File(basePath + fileName);
        if (!file.exists()) {
            throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_FILE_NOT_EXIST);
        }

        try {
            // 设置响应头
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            response.setContentType("application/octet-stream");

            // 使用流对流传输，避免一次性读取到内存
            // 使用 Hutool 的 FileUtil.writeToStream 或 Java 原生 try-with-resources
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[4096]; // 4KB 缓冲区
                int len;
                while ((len = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
                os.flush();
            }
        } catch (IOException e) {
            // 下载过程中连接断开是正常现象，通常只记录 debug
            log.debug("文件下载中断: {}", fileName);
        }
    }
}
