package com.example.activity.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PromotionSummaryTabEnum {

    DISTRICT_PROMOTED(ReviewLevelEnum.SCORE_DISTRICT, ReviewResultEnum.PROMOTED),
    CITY_PROMOTED(ReviewLevelEnum.SCORE_CITY, ReviewResultEnum.PROMOTED),
    PROVINCE_AWARD(ReviewLevelEnum.SCORE_PROVINCE, ReviewResultEnum.AWARD);

    private final ReviewLevelEnum reviewLevel;
    private final ReviewResultEnum result;
}
