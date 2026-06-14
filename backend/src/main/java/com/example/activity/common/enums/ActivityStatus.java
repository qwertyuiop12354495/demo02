package com.example.activity.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ActivityStatus {

    DRAFT("DRAFT"),
    PUBLISHED("PUBLISHED"),
    OFFLINE("OFFLINE");

    @EnumValue
    @JsonValue
    private final String value;
}
