package com.example.blog.modules.file.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.example.blog.common.constants.Constants;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.core.exception.CustomerException;
import com.example.blog.modules.file.service.FileService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Service
@Slf4j
// 只有当 file.storage-type 为 local 时，这个 Bean 才会生效
@ConditionalOnProperty(name = "file.storage-type", havingValue = "local", matchIfMissing = true)
public class LocalFileServiceImpl implements FileService {

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
        // 添加针对文件对象的防空断言校验
        Assert.notNull(file, "上传文件对象不能为空");

        if (file.isEmpty()) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_FILE_IS_EMPTY);
        }

        try {
            // 1. 生成文件名 (防止文件名冲突 & 路径遍历攻击)
            String originalFilename = file.getOriginalFilename();
            // 使用 Hutool 的 mainName 获取主文件名，防 ../../ 攻击
            String safeName = FileUtil.mainName(originalFilename);
            String extName = FileUtil.extName(originalFilename);
            String fileName = System.currentTimeMillis() + StrUtil.UNDERLINE + safeName + StrUtil.DOT + extName;

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
    public String upload(byte[] fileData, String originalFilename) {
        try {
            String extName = cn.hutool.core.io.FileUtil.extName(originalFilename);
            String safeName = cn.hutool.core.io.FileUtil.mainName(originalFilename);
            String fileName = System.currentTimeMillis() + StrUtil.UNDERLINE + safeName + StrUtil.DOT + extName;

            // 使用 Hutool 工具类直接把字节数组写成文件
            cn.hutool.core.io.FileUtil.writeBytes(fileData, basePath + fileName);

            return accessPath + fileName;
        } catch (Exception e) {
            log.error("本地字节流上传失败", e);
            throw new CustomerException(ResultCode.INTERNAL_SERVER_ERROR, MessageConstants.MSG_UPLOAD_FAILURE);
        }
    }

    @Override
    public void download(String fileName, HttpServletResponse response) {
        // 添加针对文件名和响应对象的防空断言校验
        Assert.hasText(fileName, "下载文件名不能为空");
        Assert.notNull(response, "HTTP响应对象不能为空");

        // 安全检查：防止下载 files 目录以外的文件 (如 ../application.yml)
        if (fileName.contains(StrUtil.DOUBLE_DOT) || fileName.contains(StrUtil.SLASH) || fileName.contains(StrUtil.BACKSLASH)) {
            throw new CustomerException(ResultCode.PARAM_ERROR, "非法的文件名");
        }

        File file = new File(basePath + fileName);
        if (!file.exists()) {
            throw new CustomerException(ResultCode.NOT_FOUND, MessageConstants.MSG_FILE_NOT_EXIST);
        }

        try {
            // 设置响应头
            response.addHeader(HttpHeaders.CONTENT_DISPOSITION, Constants.FILE_ATTACHMENT_HEADER_PREFIX + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);

            // 使用流对流传输，避免一次性读取到内存
            // 使用 Hutool 的 FileUtil.writeToStream 或 Java 原生 try-with-resources
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[IoUtil.DEFAULT_BUFFER_SIZE]; // 8KB 缓冲区
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

    @Override
    public int clearOrphanFiles(Set<String> activeFileNames) {
        File dir = new File(basePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return 0;
        }

        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return 0;
        }

        int deleteCount = 0;
        long currentTime = System.currentTimeMillis();

        for (File file : files) {
            if (file.isFile()) {
                // 1. 检查是否在安全期内 (创建时间不到 24 小时的，坚决不删)
                if (currentTime - file.lastModified() < Constants.FILE_SAFE_TIME_WINDOW_MS) {
                    continue;
                }

                // 2. 如果不在活跃文件集合中，执行物理删除
                if (!activeFileNames.contains(file.getName())) {
                    boolean deleted = file.delete();
                    if (deleted) {
                        deleteCount++;
                        log.debug("成功删除孤儿文件: {}", file.getName());
                    }
                }
            }
        }
        return deleteCount;
    }
}