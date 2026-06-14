package com.example.activity.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReviewLevelEnum {

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

    public static ReviewLevelEnum fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Review level must not be blank");
        }
        return ReviewLevelEnum.valueOf(value.trim());
    }

    public static ReviewLevelEnum fromWorkStep(WorkStepEnum step) {
        if (step == null) {
            throw new IllegalArgumentException("Work step must not be null");
        }
        return ReviewLevelEnum.valueOf(step.name());
    }

    public WorkStepEnum toWorkStep() {
        return WorkStepEnum.valueOf(name());
    }
}
