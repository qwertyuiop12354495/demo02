package com.example.activity.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ActivityStatusEnum {

    DRAFT("DRAFT"),
    PUBLISHED("PUBLISHED"),
    CLOSED("CLOSED");

    @EnumValue
    @JsonValue
    private final String value;

    public static ActivityStatusEnum fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Activity status must not be blank");
        }
        String normalized = value.trim();
        if ("OFFLINE".equals(normalized)) {
            return CLOSED;
        }
        return ActivityStatusEnum.valueOf(normalized);
    }

    public static ActivityStatusEnum fromLegacy(ActivityStatus legacy) {
        if (legacy == null) {
            throw new IllegalArgumentException("Legacy activity status must not be null");
        }
        return fromValue(legacy.getValue());
    }

    public ActivityStatus toLegacy() {
        if (this == CLOSED) {
            return ActivityStatus.OFFLINE;
        }
        return ActivityStatus.valueOf(name());
    }
}
