package com.example.activity.service;

import com.example.activity.common.enums.ActivityStatus;
import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;
import com.example.activity.entity.Activity;
import com.example.activity.mapper.ActivityMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityQuotaServiceTest {

    @Mock
    private ActivityMapper activityMapper;

    @InjectMocks
    private ActivityQuotaService activityQuotaService;

    @Test
    void reserveQuota_shouldSucceedWhenUpdateAffected() {
        Activity updated = new Activity();
        updated.setId(1L);
        updated.setCurrentCount(1);
        updated.setMaxCount(10);
        updated.setStatus(ActivityStatus.PUBLISHED);

        when(activityMapper.reserveQuota(1L)).thenReturn(1);
        when(activityMapper.selectById(1L)).thenReturn(updated);

        Activity result = activityQuotaService.reserveQuota(1L);

        assertEquals(1, result.getCurrentCount());
        verify(activityMapper).reserveQuota(1L);
    }

    @Test
    void reserveQuota_shouldRejectWhenNoSlotAvailable() {
        when(activityMapper.reserveQuota(1L)).thenReturn(0);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> activityQuotaService.reserveQuota(1L));
        assertEquals(ErrorCode.QUOTA_FULL.getCode(), ex.getCode());
    }

    @Test
    void releaseQuota_shouldRejectWhenCountAlreadyZero() {
        when(activityMapper.releaseQuota(1L)).thenReturn(0);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> activityQuotaService.releaseQuota(1L));
        assertEquals(ErrorCode.INTERNAL_ERROR.getCode(), ex.getCode());
    }
}
