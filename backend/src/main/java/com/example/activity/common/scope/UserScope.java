package com.example.activity.common.scope;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserScope {

    String provinceName;

    String cityName;

    String districtName;

    String schoolName;

    public static UserScope of(String provinceName, String cityName, String districtName, String schoolName) {
        return UserScope.builder()
                .provinceName(provinceName)
                .cityName(cityName)
                .districtName(districtName)
                .schoolName(schoolName)
                .build();
    }

    public static UserScope fromAuthUser(com.example.activity.common.auth.AuthUser user) {
        return UserScope.of(
                user.getProvinceName(),
                user.getCityName(),
                user.getDistrictName(),
                user.getSchoolName()
        );
    }
}
