package com.example.activity.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoleTypeEnum {

    TEACHER("TEACHER"),
    SCHOOL_ADMIN("SCHOOL_ADMIN"),
    DISTRICT_ADMIN("DISTRICT_ADMIN"),
    CITY_ADMIN("CITY_ADMIN"),
    PROVINCE_ADMIN("PROVINCE_ADMIN"),
    DISTRICT_REVIEWER("DISTRICT_REVIEWER"),
    CITY_REVIEWER("CITY_REVIEWER"),
    PROVINCE_REVIEWER("PROVINCE_REVIEWER");

    @EnumValue
    @JsonValue
    private final String value;

    public static RoleTypeEnum fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Role type must not be blank");
        }
        String normalized = value.trim();
        return switch (normalized) {
            case "USER" -> TEACHER;
            case "ADMIN" -> PROVINCE_ADMIN;
            default -> RoleTypeEnum.valueOf(normalized);
        };
    }

    public static RoleTypeEnum fromLegacyUserRole(UserRole legacyRole) {
        if (legacyRole == null) {
            throw new IllegalArgumentException("Legacy role must not be null");
        }
        return fromValue(legacyRole.getValue());
    }

    public boolean isAdminRole() {
        return this == SCHOOL_ADMIN
                || this == DISTRICT_ADMIN
                || this == CITY_ADMIN
                || this == PROVINCE_ADMIN;
    }

    public boolean isReviewerRole() {
        return this == DISTRICT_REVIEWER
                || this == CITY_REVIEWER
                || this == PROVINCE_REVIEWER;
    }

    public UserRole toLegacy() {
        return switch (this) {
            case TEACHER -> UserRole.USER;
            case SCHOOL_ADMIN, DISTRICT_ADMIN, CITY_ADMIN, PROVINCE_ADMIN -> UserRole.ADMIN;
            case DISTRICT_REVIEWER, CITY_REVIEWER, PROVINCE_REVIEWER -> UserRole.ADMIN;
        };
    }
}
