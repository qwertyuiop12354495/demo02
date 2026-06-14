package com.example.activity.common.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Data
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private static final int MIN_SECRET_LENGTH = 32;

    private String secret;

    private long expirationMs = 86400000L;

    @PostConstruct
    void validateSecret() {
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException("JWT_SECRET 未配置，请通过环境变量或 profile 配置提供");
        }
        if (secret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException("JWT_SECRET 长度不能少于 " + MIN_SECRET_LENGTH + " 个字符");
        }
    }
}
