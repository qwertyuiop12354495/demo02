package com.example.activity.common.storage;

import com.example.activity.common.config.MinioProperties;
import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URI;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MinioStorageService {

    private static final Pattern UNSAFE_CHARS = Pattern.compile("[^a-zA-Z0-9._-]");

    private final MinioProperties properties;
    private final ObjectProvider<MinioClient> minioClientProvider;

    /**
     * 上传作品材料，返回对象键（非公开 URL），入库时请存此值。
     */
    public String uploadWorkFile(Long workId, MultipartFile file) {
        requireEnabled();
        MinioClient client = requireClient();
        String objectKey = buildObjectKey(workId, file.getOriginalFilename());
        try (InputStream inputStream = file.getInputStream()) {
            client.putObject(PutObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectKey)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "文件上传失败");
        }
        return objectKey;
    }

    public void validateRegisteredUrl(Long workId, String fileUrlOrKey) {
        requireEnabled();
        String objectKey = normalizeToObjectKey(fileUrlOrKey);
        requireObjectKeyForWork(workId, objectKey);
    }

    /**
     * 将库中存储值（对象键或历史公开 URL）解析为短期可下载地址。
     */
    public String resolveDownloadUrl(String storedValue) {
        if (!StringUtils.hasText(storedValue)) {
            return storedValue;
        }
        if (!properties.isEnabled()) {
            return storedValue.trim();
        }
        String objectKey = normalizeToObjectKey(storedValue);
        return buildPresignedUrl(objectKey);
    }

    public String normalizeToObjectKey(String fileUrlOrKey) {
        if (!StringUtils.hasText(fileUrlOrKey)) {
            throw new BusinessException(ErrorCode.INVALID_FILE, "文件地址无效");
        }
        String normalized = fileUrlOrKey.trim();
        if (normalized.startsWith("works/")) {
            return normalized;
        }
        return extractObjectKeyFromUrl(normalized);
    }

    public String buildObjectKey(Long workId, String originalFileName) {
        String safeName = sanitizeFileName(originalFileName);
        return "works/" + workId + "/" + UUID.randomUUID() + "_" + safeName;
    }

    private void requireObjectKeyForWork(Long workId, String objectKey) {
        String expectedPrefix = "works/" + workId + "/";
        if (!objectKey.startsWith(expectedPrefix) || objectKey.contains("..")) {
            throw new BusinessException(ErrorCode.INVALID_FILE, "文件地址不在允许的存储范围内");
        }
    }

    private String extractObjectKeyFromUrl(String url) {
        try {
            URI uri = URI.create(url);
            String path = uri.getPath();
            if (!StringUtils.hasText(path)) {
                throw new BusinessException(ErrorCode.INVALID_FILE, "文件地址不在允许的存储范围内");
            }
            String bucket = properties.getBucket();
            String bucketPrefix = "/" + bucket + "/";
            int bucketIndex = path.indexOf(bucketPrefix);
            if (bucketIndex >= 0) {
                return path.substring(bucketIndex + bucketPrefix.length());
            }
            int worksIndex = path.indexOf("/works/");
            if (worksIndex >= 0) {
                return path.substring(worksIndex + 1);
            }
            throw new BusinessException(ErrorCode.INVALID_FILE, "文件地址不在允许的存储范围内");
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.INVALID_FILE, "文件地址无效");
        }
    }

    private String buildPresignedUrl(String objectKey) {
        MinioClient client = requireClient();
        try {
            return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(properties.getBucket())
                    .object(objectKey)
                    .expiry(properties.getPresignedExpirySeconds(), TimeUnit.SECONDS)
                    .build());
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "文件下载地址生成失败");
        }
    }

    private String sanitizeFileName(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "file";
        }
        String base = fileName.substring(fileName.lastIndexOf('/') + 1);
        base = base.substring(base.lastIndexOf('\\') + 1);
        return UNSAFE_CHARS.matcher(base).replaceAll("_").toLowerCase(Locale.ROOT);
    }

    private void requireEnabled() {
        if (!properties.isEnabled()) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "文件存储服务未启用");
        }
    }

    private MinioClient requireClient() {
        MinioClient client = minioClientProvider.getIfAvailable();
        if (client == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "文件存储客户端未配置");
        }
        return client;
    }
}
