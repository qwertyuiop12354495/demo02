package com.example.activity.service.support;

import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;

@Component
public class WorkFileValidator {

    public static final long MAX_FILE_SIZE = 52_428_800L;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx",
            "jpg", "jpeg", "png", "gif", "webp",
            "mp4", "mp3", "wav", "zip", "rar"
    );

    private static final Set<String> ALLOWED_MIME_PREFIXES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.",
            "image/",
            "video/",
            "audio/",
            "application/zip",
            "application/x-rar"
    );

    public void validateUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "上传文件不能为空");
        }
        validateMeta(file.getOriginalFilename(), file.getContentType(), file.getSize());
    }

    public void validateMeta(String fileName, String fileType, long fileSize) {
        if (!StringUtils.hasText(fileName)) {
            throw new BusinessException(ErrorCode.INVALID_FILE, "文件名无效");
        }
        if (fileSize <= 0 || fileSize > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_FILE, "文件大小不符合要求（最大50MB）");
        }
        String extension = extractExtension(fileName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.INVALID_FILE, "不支持的文件类型");
        }
        if (StringUtils.hasText(fileType) && !isAllowedMime(fileType)) {
            throw new BusinessException(ErrorCode.INVALID_FILE, "不支持的文件MIME类型");
        }
    }

    private String extractExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private boolean isAllowedMime(String mime) {
        String normalized = mime.toLowerCase(Locale.ROOT);
        return ALLOWED_MIME_PREFIXES.stream().anyMatch(normalized::startsWith);
    }
}
