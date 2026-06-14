package com.example.activity.common.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkFileUrlResolver {

    private final MinioStorageService minioStorageService;

    public String resolveForDownload(String storedValue) {
        return minioStorageService.resolveDownloadUrl(storedValue);
    }
}
