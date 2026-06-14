package com.example.activity.common.auth;

import com.example.activity.common.enums.RoleTypeEnum;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthUser {

    Long userId;

    String username;

    RoleTypeEnum roleType;

    String provinceName;

    String cityName;

    String districtName;

    String schoolName;

    public static AuthUser of(Long userId, String username, RoleTypeEnum roleType,
                              String provinceName, String cityName, String districtName, String schoolName) {
        return AuthUser.builder()
                .userId(userId)
                .username(username)
                .roleType(roleType)
                .provinceName(provinceName)
                .cityName(cityName)
                .districtName(districtName)
                .schoolName(schoolName)
                .build();
    }
}
