package com.example.activity.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.minio")
public class MinioProperties {

    private boolean enabled = false;

    private String endpoint = "http://127.0.0.1:9000";

    private String accessKey = "";

    private String secretKey = "";

    private String bucket = "activity-works";

    /** 预签名下载 URL 有效期（秒） */
    private int presignedExpirySeconds = 3600;
}
