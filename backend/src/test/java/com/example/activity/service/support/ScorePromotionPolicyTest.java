package com.example.activity.service.support;

import com.example.activity.common.enums.FinalResultEnum;
import com.example.activity.common.enums.WorkStatusEnum;
import com.example.activity.common.enums.WorkStepEnum;
import com.example.activity.entity.Work;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScorePromotionPolicyTest {

    private ScorePromotionPolicy policy;
    private Work work;

    @BeforeEach
    void setUp() {
        policy = new ScorePromotionPolicy();
        work = new Work();
        work.setCurrentStep(WorkStepEnum.SCORE_DISTRICT);
        work.setCurrentStatus(WorkStatusEnum.SUBMITTED);
        work.setFinalResult(FinalResultEnum.PENDING);
    }

    @Test
    void average_shouldCalculateArithmeticMean() {
        BigDecimal avg = policy.average(List.of(new BigDecimal("60"), new BigDecimal("70")));
        assertEquals(0, new BigDecimal("65.00").compareTo(avg));
    }

    @Test
    void applyAfterAllScored_districtAbove60_shouldPromoteToCity() {
        policy.applyAfterAllScored(work, WorkStepEnum.SCORE_DISTRICT, new BigDecimal("65"));
        assertEquals(WorkStepEnum.SCORE_CITY, work.getCurrentStep());
        assertEquals(WorkStatusEnum.SUBMITTED, work.getCurrentStatus());
        assertEquals(FinalResultEnum.PENDING, work.getFinalResult());
    }

    @Test
    void applyAfterAllScored_districtAt60_shouldEliminate() {
        policy.applyAfterAllScored(work, WorkStepEnum.SCORE_DISTRICT, new BigDecimal("60"));
        assertEquals(WorkStepEnum.COMPLETED, work.getCurrentStep());
        assertEquals(FinalResultEnum.ELIMINATED, work.getFinalResult());
    }

    @Test
    void applyAfterAllScored_provinceAbove90_shouldAward() {
        work.setCurrentStep(WorkStepEnum.SCORE_PROVINCE);
        policy.applyAfterAllScored(work, WorkStepEnum.SCORE_PROVINCE, new BigDecimal("91"));
        assertEquals(WorkStepEnum.COMPLETED, work.getCurrentStep());
        assertEquals(WorkStatusEnum.APPROVED, work.getCurrentStatus());
        assertEquals(FinalResultEnum.AWARD, work.getFinalResult());
    }

    @Test
    void applyAfterAllScored_provinceAt90_shouldNotAward() {
        work.setCurrentStep(WorkStepEnum.SCORE_PROVINCE);
        policy.applyAfterAllScored(work, WorkStepEnum.SCORE_PROVINCE, new BigDecimal("90"));
        assertEquals(WorkStepEnum.COMPLETED, work.getCurrentStep());
        assertEquals(WorkStatusEnum.SUBMITTED, work.getCurrentStatus());
        assertEquals(FinalResultEnum.NOT_AWARDED, work.getFinalResult());
    }
}
