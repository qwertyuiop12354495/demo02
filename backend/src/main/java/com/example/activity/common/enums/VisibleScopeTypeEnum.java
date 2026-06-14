package com.example.activity.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VisibleScopeTypeEnum {

    PUBLIC("PUBLIC"),
    PROVINCE("PROVINCE"),
    CITY("CITY"),
    DISTRICT("DISTRICT"),
    SCHOOL("SCHOOL");

    @EnumValue
    @JsonValue
    private final String value;
}
