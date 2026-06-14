package com.example.activity.service.support;

import com.example.activity.common.enums.ReviewLevelEnum;
import com.example.activity.common.enums.ReviewResultEnum;
import com.example.activity.common.enums.WorkStepEnum;
import com.example.activity.mapper.ReviewRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class ReviewRecordResultUpdater {

    private static final BigDecimal DISTRICT_CITY_THRESHOLD = new BigDecimal("60");
    private static final BigDecimal PROVINCE_AWARD_THRESHOLD = new BigDecimal("90");

    private final ReviewRecordMapper reviewRecordMapper;

    public ReviewResultEnum resolveLevelResult(WorkStepEnum scoreStep, BigDecimal averageScore) {
        return switch (scoreStep) {
            case SCORE_DISTRICT, SCORE_CITY -> averageScore.compareTo(DISTRICT_CITY_THRESHOLD) > 0
                    ? ReviewResultEnum.PROMOTED
                    : ReviewResultEnum.ELIMINATED;
            case SCORE_PROVINCE -> averageScore.compareTo(PROVINCE_AWARD_THRESHOLD) > 0
                    ? ReviewResultEnum.AWARD
                    : ReviewResultEnum.NOT_AWARDED;
            default -> throw new IllegalArgumentException("Unsupported score step: " + scoreStep);
        };
    }

    public void updateResults(Long workId, ReviewLevelEnum reviewLevel, ReviewResultEnum result) {
        reviewRecordMapper.updateResultByWorkAndLevel(workId, reviewLevel.getValue(), result.getValue());
    }
}
