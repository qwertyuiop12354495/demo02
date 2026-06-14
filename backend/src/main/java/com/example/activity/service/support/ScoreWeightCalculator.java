package com.example.activity.service.support;

import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;
import com.example.activity.entity.Activity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class ScoreWeightCalculator {

    private static final BigDecimal DEFAULT_MANUAL_WEIGHT = BigDecimal.ONE;
    private static final BigDecimal DEFAULT_AI_WEIGHT = BigDecimal.ZERO;

    public BigDecimal calculate(Activity activity, BigDecimal manualScore, BigDecimal aiScore) {
        if (manualScore == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "人工分不能为空");
        }
        validateScoreRange(manualScore, "人工分");
        BigDecimal normalizedAi = aiScore == null ? BigDecimal.ZERO : aiScore;
        validateScoreRange(normalizedAi, "AI分");

        BigDecimal manualWeight = activity != null && activity.getManualScoreWeight() != null
                ? activity.getManualScoreWeight() : DEFAULT_MANUAL_WEIGHT;
        BigDecimal aiWeight = activity != null && activity.getAiScoreWeight() != null
                ? activity.getAiScoreWeight() : DEFAULT_AI_WEIGHT;

        return manualScore.multiply(manualWeight)
                .add(normalizedAi.multiply(aiWeight))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void validateScoreRange(BigDecimal score, String label) {
        if (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, label + "必须在0-100之间");
        }
    }
}
