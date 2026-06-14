package com.example.activity.service;

import com.example.activity.common.auth.AuthUser;
import com.example.activity.common.enums.ActivityStatus;
import com.example.activity.common.enums.PublishableActivityStatus;
import com.example.activity.common.enums.RoleTypeEnum;
import com.example.activity.common.exception.BusinessException;
import com.example.activity.converter.ActivityConverter;
import com.example.activity.service.support.ActivityAdminAccessGuard;
import com.example.activity.dto.request.activity.ActivityStatusUpdateRequest;
import com.example.activity.entity.Activity;
import com.example.activity.mapper.ActivityMapper;
import com.example.activity.mapper.ActivityRegistrationMapper;
import com.example.activity.service.impl.ActivityServiceImpl;
import com.example.activity.vo.activity.ActivityVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityServiceImplTest {

    @Mock
    private ActivityMapper activityMapper;

    @Mock
    private ActivityRegistrationMapper activityRegistrationMapper;

    @Spy
    private ActivityConverter activityConverter = new ActivityConverter();

    @Mock
    private ActivityAdminAccessGuard activityAdminAccessGuard;

    @InjectMocks
    private ActivityServiceImpl activityService;

    private final AuthUser provinceAdmin = AuthUser.of(
            1L, "province-admin", RoleTypeEnum.PROVINCE_ADMIN, "广东省", null, null, null);

    @org.junit.jupiter.api.BeforeEach
    void setUpAdminGuard() {
        lenient().when(activityAdminAccessGuard.requireProvinceAdmin()).thenReturn(provinceAdmin);
        lenient().doAnswer(invocation -> null).when(activityAdminAccessGuard).requireActivityInProvince(any(), any());
        lenient().doAnswer(invocation -> null).when(activityAdminAccessGuard).applyProvinceScope(any(), any());
    }

    @Test
    void updateActivityStatus_shouldRejectDraftToOffline() {
        Activity activity = new Activity();
        activity.setId(1L);
        activity.setTitle("测试活动");
        activity.setStatus(ActivityStatus.DRAFT);
        when(activityMapper.selectById(1L)).thenReturn(activity);

        ActivityStatusUpdateRequest request = new ActivityStatusUpdateRequest();
        request.setStatus(PublishableActivityStatus.OFFLINE);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> activityService.updateActivityStatus(1L, request));
        assertEquals(40006, ex.getCode());
    }

    @Test
    void getPublishedActivityDetail_shouldNotThrowWhenSignupFieldsIncomplete() {
        Activity activity = publishedActivityWithIncompleteSignup();
        when(activityMapper.selectById(1L)).thenReturn(activity);
        when(activityRegistrationMapper.selectOne(org.mockito.ArgumentMatchers.any())).thenReturn(null);

        ActivityVO vo = activityService.getPublishedActivityDetail(1L, 2L);

        assertFalse(vo.getCanRegister());
        assertEquals("活动信息不完整", vo.getRegisterDisabledReason());
    }

    @Test
    void updateActivityStatus_shouldRejectNullCurrentStatus() {
        Activity activity = new Activity();
        activity.setId(1L);
        activity.setTitle("测试活动");
        activity.setStatus(null);
        when(activityMapper.selectById(1L)).thenReturn(activity);

        ActivityStatusUpdateRequest request = new ActivityStatusUpdateRequest();
        request.setStatus(PublishableActivityStatus.PUBLISHED);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> activityService.updateActivityStatus(1L, request));
        assertEquals(40007, ex.getCode());
    }

    private Activity publishedActivityWithIncompleteSignup() {
        Activity activity = new Activity();
        activity.setId(1L);
        activity.setTitle("测试活动");
        activity.setStatus(ActivityStatus.PUBLISHED);
        activity.setSignupStartTime(null);
        activity.setSignupEndTime(LocalDateTime.now().plusDays(1));
        activity.setCurrentCount(0);
        activity.setMaxCount(10);
        return activity;
    }
}
