package com.example.blog.modules.file.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.example.blog.common.constants.Constants;
import com.example.blog.common.constants.MessageConstants;
import com.example.blog.common.enums.ResultCode;
import com.example.blog.core.exception.CustomerException;
import com.example.blog.modules.file.service.FileService;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Service
@Slf4j
// 只有当 file.storage-type 为 qiniu 时，这个 Bean 才会生效
@ConditionalOnProperty(name = "file.storage-type", havingValue = "qiniu")
public class QiniuFileServiceImpl implements FileService {

    @Value("${qiniu.access-key}")
    private String accessKey;

    @Value("${qiniu.secret-key}")
    private String secretKey;

    @Value("${qiniu.bucket}")
    private String bucket;

    @Value("${qiniu.domain}")
    private String domain;

    @Override
    public String upload(MultipartFile file, String dir) {
        Assert.notNull(file, "上传文件对象不能为空");
        if (file.isEmpty()) {
            throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_FILE_IS_EMPTY);
        }

        try {
            // 1. 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extName = FileUtil.extName(originalFilename);
            String safeName = FileUtil.mainName(originalFilename);

            // 动态拼接前缀：blog/cover/ 或者 blog/avatar/
            String currentPrefix = Constants.QINIU_DIR_PREFIX;
            if (StrUtil.isNotBlank(dir)) {
                currentPrefix = currentPrefix + StrUtil.SLASH + dir;
            }
            String fileName = currentPrefix + StrUtil.SLASH + System.currentTimeMillis() + StrUtil.UNDERLINE + safeName + StrUtil.DOT + extName;

            // 2. 构造七牛云认证与上传管理器 (建议提取到配置类做成单例，这里为了演示写在一起)
            Configuration cfg = new Configuration(Region.autoRegion());
            UploadManager uploadManager = new UploadManager(cfg);
            Auth auth = Auth.create(accessKey, secretKey);
            String upToken = auth.uploadToken(bucket);

            // 3. 上传到七牛云
            uploadManager.put(file.getInputStream(), fileName, upToken, null, null);

            // 4. 返回 CDN 访问链接
            String finalDomain = domain.endsWith(StrUtil.SLASH) ? domain : domain + StrUtil.SLASH;
            return finalDomain + fileName;

        } catch (Exception e) {
            log.error("七牛云文件上传失败", e);
            throw new CustomerException(ResultCode.INTERNAL_SERVER_ERROR, MessageConstants.MSG_UPLOAD_FAILURE);
        }
    }

    @Override
    public String upload(byte[] fileData, String originalFilename, String dir) {
        try {
            String extName = FileUtil.extName(originalFilename);
            String safeName = FileUtil.mainName(originalFilename);

            String currentPrefix = Constants.QINIU_DIR_PREFIX;
            if (StrUtil.isNotBlank(dir)) {
                currentPrefix = currentPrefix + StrUtil.SLASH + dir;
            }
            String fileName = currentPrefix + StrUtil.SLASH + System.currentTimeMillis() + StrUtil.UNDERLINE + safeName + StrUtil.DOT + extName;

            Configuration cfg = new Configuration(Region.autoRegion());
            UploadManager uploadManager = new UploadManager(cfg);
            Auth auth = Auth.create(accessKey, secretKey);
            String upToken = auth.uploadToken(bucket);

            // 七牛云 SDK 支持直接 put 字节数组！效率极高
            uploadManager.put(fileData, fileName, upToken, null, null, true);

            String finalDomain = domain.endsWith(StrUtil.SLASH) ? domain : domain + StrUtil.SLASH;
            return finalDomain + fileName;
        } catch (Exception e) {
            log.error("七牛云字节流上传失败", e);
            throw new CustomerException(ResultCode.INTERNAL_SERVER_ERROR, MessageConstants.MSG_UPLOAD_FAILURE);
        }
    }

    @Override
    public void download(String fileName, HttpServletResponse response) {
        // 注意：使用了七牛云后，前端直接访问 CDN 链接即可下载或查看，后端通常不需要再做流转发。
        // 如果一定要强管控，可以生成七牛云的私有下载链接并重定向。
        // 这里抛出异常，强制前端走直连。
        throw new CustomerException(ResultCode.PARAM_ERROR, MessageConstants.MSG_OSS_DIRECT_ACCESS_REQUIRED);
    }

    @Override
    public int clearOrphanFiles(Set<String> activeFileNames) {
        // 七牛云通常不建议在业务服务器上做高频的文件比对和删除（网络IO极高）。
        // 方案A：忽略此操作，云存储很便宜。
        // 方案B：使用七牛云的 BucketManager 拉取文件列表进行对比（需另写逻辑）。
        log.info("七牛云模式下暂不执行本地孤儿文件清理逻辑");
        return 0;
    }
}