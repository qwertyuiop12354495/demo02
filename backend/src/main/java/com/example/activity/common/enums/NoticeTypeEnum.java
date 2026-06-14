package com.example.activity.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NoticeTypeEnum {

    REVIEW_RESULT("REVIEW_RESULT"),
    SCORE_RESULT("SCORE_RESULT"),
    AWARD_LIST("AWARD_LIST"),
    GENERAL("GENERAL");

    @EnumValue
    @JsonValue
    private final String value;
}
