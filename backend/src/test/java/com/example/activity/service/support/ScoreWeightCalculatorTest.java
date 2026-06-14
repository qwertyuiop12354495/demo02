package com.example.activity.service.support;

import com.example.activity.entity.Activity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScoreWeightCalculatorTest {

    private final ScoreWeightCalculator calculator = new ScoreWeightCalculator();

    @Test
    void calculate_shouldApplyActivityWeights() {
        Activity activity = new Activity();
        activity.setManualScoreWeight(new BigDecimal("0.7"));
        activity.setAiScoreWeight(new BigDecimal("0.3"));

        BigDecimal result = calculator.calculate(
                activity, new BigDecimal("80"), new BigDecimal("90"));

        assertEquals(0, new BigDecimal("83.00").compareTo(result));
    }

    @Test
    void calculate_shouldDefaultToManualOnlyWhenWeightsMissing() {
        BigDecimal result = calculator.calculate(null, new BigDecimal("85"), null);
        assertEquals(0, new BigDecimal("85.00").compareTo(result));
    }
}
