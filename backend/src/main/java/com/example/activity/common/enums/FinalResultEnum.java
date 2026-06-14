package com.example.activity.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FinalResultEnum {

    PENDING("PENDING"),
    PROMOTED("PROMOTED"),
    ELIMINATED("ELIMINATED"),
    AWARD("AWARD"),
    NOT_AWARDED("NOT_AWARDED");

    @EnumValue
    @JsonValue
    private final String value;

    public static FinalResultEnum fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Final result must not be blank");
        }
        return FinalResultEnum.valueOf(value.trim());
    }
}
