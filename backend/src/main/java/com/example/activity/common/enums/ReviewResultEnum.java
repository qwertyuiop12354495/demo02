package com.example.activity.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReviewResultEnum {

    PENDING("PENDING"),
    APPROVED("APPROVED"),
    REVISION("REVISION"),
    ELIMINATED("ELIMINATED"),
    PROMOTED("PROMOTED"),
    AWARD("AWARD"),
    NOT_AWARDED("NOT_AWARDED");

    @EnumValue
    @JsonValue
    private final String value;
}
