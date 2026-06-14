package com.example.activity.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WorkStepEnum {

    SCHOOL("SCHOOL"),
    DISTRICT("DISTRICT"),
    CITY("CITY"),
    PROVINCE("PROVINCE"),
    SCORE_DISTRICT("SCORE_DISTRICT"),
    SCORE_CITY("SCORE_CITY"),
    SCORE_PROVINCE("SCORE_PROVINCE"),
    COMPLETED("COMPLETED");

    @EnumValue
    @JsonValue
    private final String value;

    public static WorkStepEnum fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Work step must not be blank");
        }
        return WorkStepEnum.valueOf(value.trim());
    }
}
