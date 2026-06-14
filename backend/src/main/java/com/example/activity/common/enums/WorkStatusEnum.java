package com.example.activity.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WorkStatusEnum {

    DRAFT("DRAFT"),
    SUBMITTED("SUBMITTED"),
    REVISION_REQUIRED("REVISION_REQUIRED"),
    APPROVED("APPROVED");

    @EnumValue
    @JsonValue
    private final String value;

    public static WorkStatusEnum fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Work status must not be blank");
        }
        return WorkStatusEnum.valueOf(value.trim());
    }
}
