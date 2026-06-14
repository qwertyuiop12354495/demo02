package com.example.activity.service.support;

import com.example.activity.common.enums.ReviewResultEnum;
import com.example.activity.common.enums.WorkStepEnum;
import com.example.activity.mapper.ReviewRecordMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReviewRecordResultUpdaterTest {

    @Mock
    private ReviewRecordMapper reviewRecordMapper;

    @InjectMocks
    private ReviewRecordResultUpdater updater;

    @Test
    void resolveLevelResult_districtAbove60_shouldPromote() {
        ReviewResultEnum result = updater.resolveLevelResult(
                WorkStepEnum.SCORE_DISTRICT, new BigDecimal("61"));
        assertEquals(ReviewResultEnum.PROMOTED, result);
    }

    @Test
    void resolveLevelResult_provinceAbove90_shouldAward() {
        ReviewResultEnum result = updater.resolveLevelResult(
                WorkStepEnum.SCORE_PROVINCE, new BigDecimal("91"));
        assertEquals(ReviewResultEnum.AWARD, result);
    }

    @Test
    void updateResults_shouldCallMapper() {
        updater.updateResults(1L,
                com.example.activity.common.enums.ReviewLevelEnum.SCORE_DISTRICT,
                ReviewResultEnum.PROMOTED);
        verify(reviewRecordMapper).updateResultByWorkAndLevel(1L, "SCORE_DISTRICT", "PROMOTED");
    }
}
