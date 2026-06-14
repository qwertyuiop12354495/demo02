package com.example.activity.common.auth;

import com.example.activity.common.config.JwtProperties;
import com.example.activity.common.enums.RoleTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("test-secret-key-at-least-32-characters-long");
        properties.setExpirationMs(3600_000L);
        jwtTokenProvider = new JwtTokenProvider(properties);
    }

    @Test
    void generateAndParse_shouldRoundTripScopeClaims() {
        AuthUser original = AuthUser.of(
                5L,
                "teacher1",
                RoleTypeEnum.TEACHER,
                "广东省",
                "深圳市",
                "南山区",
                "示例中学"
        );

        String token = jwtTokenProvider.generateToken(original);
        AuthUser parsed = jwtTokenProvider.parseToken(token);

        assertEquals(original.getUserId(), parsed.getUserId());
        assertEquals(original.getUsername(), parsed.getUsername());
        assertEquals(original.getRoleType(), parsed.getRoleType());
        assertEquals(original.getProvinceName(), parsed.getProvinceName());
        assertEquals(original.getCityName(), parsed.getCityName());
        assertEquals(original.getDistrictName(), parsed.getDistrictName());
        assertEquals(original.getSchoolName(), parsed.getSchoolName());
    }
}
