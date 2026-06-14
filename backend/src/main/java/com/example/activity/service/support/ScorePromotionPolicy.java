package com.example.activity.service.support;

import com.example.activity.common.enums.FinalResultEnum;
import com.example.activity.common.enums.WorkStatusEnum;
import com.example.activity.common.enums.WorkStepEnum;
import com.example.activity.entity.Work;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class ScorePromotionPolicy {

    private static final BigDecimal DISTRICT_CITY_THRESHOLD = new BigDecimal("60");
    private static final BigDecimal PROVINCE_AWARD_THRESHOLD = new BigDecimal("90");

    public BigDecimal average(List<BigDecimal> scores) {
        if (scores == null || scores.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
    }

    public void applyAfterAllScored(Work work, WorkStepEnum scoredLevel, BigDecimal averageScore) {
        work.setFinalScore(averageScore);
        switch (scoredLevel) {
            case SCORE_DISTRICT -> applyDistrictOrCity(work, averageScore, WorkStepEnum.SCORE_CITY);
            case SCORE_CITY -> applyDistrictOrCity(work, averageScore, WorkStepEnum.SCORE_PROVINCE);
            case SCORE_PROVINCE -> applyProvince(work, averageScore);
            default -> throw new IllegalArgumentException("Unsupported score level: " + scoredLevel);
        }
    }

    private void applyDistrictOrCity(Work work, BigDecimal averageScore, WorkStepEnum nextStep) {
        if (averageScore.compareTo(DISTRICT_CITY_THRESHOLD) > 0) {
            work.setCurrentStep(nextStep);
            work.setCurrentStatus(WorkStatusEnum.SUBMITTED);
            work.setFinalResult(FinalResultEnum.PENDING);
        } else {
            work.setCurrentStep(WorkStepEnum.COMPLETED);
            work.setCurrentStatus(WorkStatusEnum.SUBMITTED);
            work.setFinalResult(FinalResultEnum.ELIMINATED);
        }
    }

    private void applyProvince(Work work, BigDecimal averageScore) {
        work.setCurrentStep(WorkStepEnum.COMPLETED);
        if (averageScore.compareTo(PROVINCE_AWARD_THRESHOLD) > 0) {
            work.setCurrentStatus(WorkStatusEnum.APPROVED);
            work.setFinalResult(FinalResultEnum.AWARD);
        } else {
            work.setCurrentStatus(WorkStatusEnum.SUBMITTED);
            work.setFinalResult(FinalResultEnum.NOT_AWARDED);
        }
    }
}
