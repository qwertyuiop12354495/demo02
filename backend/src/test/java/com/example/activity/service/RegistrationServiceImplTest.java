package com.example.activity.service;

import com.example.activity.common.auth.AuthContext;
import com.example.activity.common.auth.AuthUser;
import com.example.activity.common.enums.ActivityStatus;
import com.example.activity.common.enums.RoleTypeEnum;
import com.example.activity.common.enums.RegistrationStatus;
import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;
import com.example.activity.converter.RegistrationConverter;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.activity.dto.query.AdminRegistrationPageQuery;
import com.example.activity.dto.request.registration.RegistrationAuditRequest;
import com.example.activity.dto.request.registration.RegistrationCreateRequest;
import com.example.activity.entity.Activity;
import com.example.activity.entity.ActivityRegistration;
import com.example.activity.entity.SysUser;
import com.example.activity.mapper.ActivityMapper;
import com.example.activity.mapper.ActivityRegistrationMapper;
import com.example.activity.mapper.SysUserMapper;
import com.example.activity.service.impl.RegistrationServiceImpl;
import com.example.activity.service.support.ActivityAdminAccessGuard;
import com.example.activity.service.support.RegistrationScopeQueryBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceImplTest {

    @Mock
    private ActivityMapper activityMapper;

    @Mock
    private ActivityRegistrationMapper activityRegistrationMapper;

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private ActivityQuotaService activityQuotaService;

    @Spy
    private RegistrationConverter registrationConverter = new RegistrationConverter();

    @Mock
    private ActivityAdminAccessGuard activityAdminAccessGuard;

    @Mock
    private RegistrationScopeQueryBuilder registrationScopeQueryBuilder;

    @InjectMocks
    private RegistrationServiceImpl registrationService;

    private final AuthUser schoolAdmin = AuthUser.of(
            1L, "school-admin", RoleTypeEnum.SCHOOL_ADMIN,
            "广东省", "深圳市", "南山区", "实验小学");

    private Activity publishedActivity;
    private Activity reservedActivity;
    private RegistrationCreateRequest createRequest;

    @AfterEach
    void tearDown() {
        AuthContext.clear();
    }

    @BeforeEach
    void setUp() {
        publishedActivity = new Activity();
        publishedActivity.setId(1L);
        publishedActivity.setTitle("测试活动");
        publishedActivity.setStatus(ActivityStatus.PUBLISHED);
        publishedActivity.setSignupStartTime(LocalDateTime.now().minusHours(1));
        publishedActivity.setSignupEndTime(LocalDateTime.now().plusDays(1));
        publishedActivity.setMaxCount(2);
        publishedActivity.setCurrentCount(0);

        reservedActivity = new Activity();
        reservedActivity.setId(1L);
        reservedActivity.setTitle("测试活动");
        reservedActivity.setStatus(ActivityStatus.PUBLISHED);
        reservedActivity.setMaxCount(2);
        reservedActivity.setCurrentCount(1);

        lenient().when(activityAdminAccessGuard.requireProvinceAdmin()).thenReturn(
                AuthUser.of(99L, "province-admin", RoleTypeEnum.PROVINCE_ADMIN, "广东省", null, null, null));

        createRequest = new RegistrationCreateRequest();
        createRequest.setActivityId(1L);
        createRequest.setRemark("期待参加");
    }

    @Test
    void register_shouldReserveQuotaBeforeInsert() {
        when(activityQuotaService.lockActivity(1L)).thenReturn(publishedActivity);
        when(activityRegistrationMapper.selectOne(any())).thenReturn(null);
        when(activityQuotaService.reserveQuota(1L)).thenReturn(reservedActivity);
        when(activityRegistrationMapper.insert(any(ActivityRegistration.class))).thenAnswer(invocation -> {
            ActivityRegistration registration = invocation.getArgument(0);
            registration.setId(10L);
            return 1;
        });

        var result = registrationService.register(createRequest, 2L);

        assertEquals(10L, result.getId());
        assertEquals(RegistrationStatus.PENDING, result.getStatus());
        verify(activityQuotaService).reserveQuota(1L);
        verify(activityRegistrationMapper).insert(any(ActivityRegistration.class));
    }

    @Test
    void register_shouldRejectWhenActivityNotFound() {
        when(activityQuotaService.lockActivity(1L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> registrationService.register(createRequest, 2L));
        assertEquals(ErrorCode.NOT_FOUND.getCode(), ex.getCode());
        verify(activityQuotaService, never()).reserveQuota(1L);
        verify(activityRegistrationMapper, never()).insert(any(ActivityRegistration.class));
    }

    @Test
    void register_shouldRejectWhenActivityOffline() {
        publishedActivity.setStatus(ActivityStatus.OFFLINE);
        when(activityQuotaService.lockActivity(1L)).thenReturn(publishedActivity);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> registrationService.register(createRequest, 2L));
        assertEquals(ErrorCode.ACTIVITY_OFFLINE.getCode(), ex.getCode());
        verify(activityQuotaService, never()).reserveQuota(1L);
    }

    @Test
    void register_shouldRejectWhenRegistrationNotStarted() {
        publishedActivity.setSignupStartTime(LocalDateTime.now().plusHours(1));
        when(activityQuotaService.lockActivity(1L)).thenReturn(publishedActivity);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> registrationService.register(createRequest, 2L));
        assertEquals(ErrorCode.REGISTRATION_NOT_STARTED.getCode(), ex.getCode());
    }

    @Test
    void register_shouldRejectWhenRegistrationClosed() {
        publishedActivity.setSignupEndTime(LocalDateTime.now().minusMinutes(1));
        when(activityQuotaService.lockActivity(1L)).thenReturn(publishedActivity);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> registrationService.register(createRequest, 2L));
        assertEquals(ErrorCode.REGISTRATION_CLOSED.getCode(), ex.getCode());
    }

    @Test
    void register_shouldRejectWhenQuotaReserveFails() {
        when(activityQuotaService.lockActivity(1L)).thenReturn(publishedActivity);
        when(activityRegistrationMapper.selectOne(any())).thenReturn(null);
        when(activityQuotaService.reserveQuota(1L))
                .thenThrow(new BusinessException(ErrorCode.QUOTA_FULL));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> registrationService.register(createRequest, 2L));
        assertEquals(ErrorCode.QUOTA_FULL.getCode(), ex.getCode());
        verify(activityRegistrationMapper, never()).insert(any(ActivityRegistration.class));
    }

    @Test
    void register_shouldRejectWhenAlreadyRegistered() {
        when(activityQuotaService.lockActivity(1L)).thenReturn(publishedActivity);
        when(activityRegistrationMapper.selectOne(any())).thenReturn(new ActivityRegistration());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> registrationService.register(createRequest, 2L));
        assertEquals(ErrorCode.DUPLICATE_REGISTRATION.getCode(), ex.getCode());
        verify(activityQuotaService, never()).reserveQuota(1L);
    }

    @Test
    void register_shouldRollbackWhenInsertFailsAfterReserve() {
        when(activityQuotaService.lockActivity(1L)).thenReturn(publishedActivity);
        when(activityRegistrationMapper.selectOne(any())).thenReturn(null);
        when(activityQuotaService.reserveQuota(1L)).thenReturn(reservedActivity);
        when(activityRegistrationMapper.insert(any(ActivityRegistration.class)))
                .thenThrow(new DuplicateKeyException("duplicate"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> registrationService.register(createRequest, 2L));
        assertEquals(ErrorCode.DUPLICATE_REGISTRATION.getCode(), ex.getCode());
    }

    @Test
    void cancel_shouldReleaseQuotaAfterStatusUpdate() {
        ActivityRegistration registration = pendingRegistration(10L, 2L, 1L);
        when(activityRegistrationMapper.selectByIdForUpdate(10L)).thenReturn(registration);
        when(activityQuotaService.lockActivity(1L)).thenReturn(publishedActivity);

        var result = registrationService.cancel(10L, 2L);

        assertEquals(RegistrationStatus.CANCELLED, result.getStatus());
        verify(activityQuotaService).releaseQuota(1L);
    }

    @Test
    void cancel_shouldRejectWhenNotOwner() {
        ActivityRegistration registration = pendingRegistration(10L, 3L, 1L);
        when(activityRegistrationMapper.selectByIdForUpdate(10L)).thenReturn(registration);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> registrationService.cancel(10L, 2L));
        assertEquals(ErrorCode.FORBIDDEN.getCode(), ex.getCode());
        verify(activityQuotaService, never()).releaseQuota(1L);
    }

    @Test
    void cancel_shouldRejectWhenAlreadyCancelled() {
        ActivityRegistration registration = pendingRegistration(10L, 2L, 1L);
        registration.setStatus(RegistrationStatus.CANCELLED);
        when(activityRegistrationMapper.selectByIdForUpdate(10L)).thenReturn(registration);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> registrationService.cancel(10L, 2L));
        assertEquals(ErrorCode.INVALID_STATUS.getCode(), ex.getCode());
    }

    @Test
    void auditApprove_shouldUpdateStatusWithoutReleasingQuota() {
        AuthContext.set(schoolAdmin);
        ActivityRegistration registration = pendingRegistration(10L, 2L, 1L);
        when(activityRegistrationMapper.selectByIdForUpdate(10L)).thenReturn(registration);
        when(activityQuotaService.lockActivity(1L)).thenReturn(publishedActivity);
        when(sysUserMapper.selectById(2L)).thenReturn(registrantUser());

        RegistrationAuditRequest request = new RegistrationAuditRequest();
        request.setAction("APPROVE");

        var result = registrationService.audit(10L, request, 1L);

        assertEquals(RegistrationStatus.APPROVED, result.getStatus());
        assertEquals(1L, result.getAuditedBy());
        verify(activityQuotaService, never()).releaseQuota(1L);
        verify(activityRegistrationMapper).updateById(registration);
    }

    @Test
    void auditReject_shouldReleaseQuota() {
        AuthContext.set(schoolAdmin);
        ActivityRegistration registration = pendingRegistration(10L, 2L, 1L);
        when(activityRegistrationMapper.selectByIdForUpdate(10L)).thenReturn(registration);
        when(activityQuotaService.lockActivity(1L)).thenReturn(publishedActivity);
        when(sysUserMapper.selectById(2L)).thenReturn(registrantUser());

        RegistrationAuditRequest request = new RegistrationAuditRequest();
        request.setAction("REJECT");
        request.setAuditRemark("名额已满");

        var result = registrationService.audit(10L, request, 1L);

        assertEquals(RegistrationStatus.REJECTED, result.getStatus());
        assertEquals("名额已满", result.getAuditRemark());
        verify(activityQuotaService).releaseQuota(1L);
    }

    @Test
    void audit_shouldRejectWhenNotPending() {
        AuthContext.set(schoolAdmin);
        ActivityRegistration registration = pendingRegistration(10L, 2L, 1L);
        registration.setStatus(RegistrationStatus.APPROVED);
        when(activityRegistrationMapper.selectByIdForUpdate(10L)).thenReturn(registration);

        RegistrationAuditRequest request = new RegistrationAuditRequest();
        request.setAction("APPROVE");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> registrationService.audit(10L, request, 1L));
        assertEquals(ErrorCode.INVALID_STATUS.getCode(), ex.getCode());
        verify(activityQuotaService, never()).releaseQuota(1L);
    }

    @Test
    void audit_shouldRejectWhenActionInvalid() {
        AuthContext.set(schoolAdmin);
        RegistrationAuditRequest request = new RegistrationAuditRequest();
        request.setAction("INVALID");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> registrationService.audit(10L, request, 1L));
        assertEquals(ErrorCode.VALIDATION_FAILED.getCode(), ex.getCode());
        verify(activityRegistrationMapper, never()).selectByIdForUpdate(10L);
    }

    @Test
    void pageByActivity_shouldRejectWhenActivityNotFound() {
        when(activityMapper.selectById(99L)).thenReturn(null);

        AdminRegistrationPageQuery query = new AdminRegistrationPageQuery();
        BusinessException ex = assertThrows(BusinessException.class,
                () -> registrationService.pageByActivity(99L, query));
        assertEquals(ErrorCode.NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void pageByActivity_shouldReturnActivitySummaryAndUserInfo() {
        when(activityMapper.selectById(1L)).thenReturn(publishedActivity);
        publishedActivity.setCreatedBy(99L);

        ActivityRegistration registration = pendingRegistration(10L, 2L, 1L);
        registration.setApplyRemark("期待参加");
        Page<ActivityRegistration> page = new Page<>(1, 10, 1);
        page.setRecords(java.util.List.of(registration));
        when(activityRegistrationMapper.selectPage(any(Page.class), any())).thenReturn(page);
        when(activityRegistrationMapper.selectCount(any())).thenReturn(1L);

        SysUser user = new SysUser();
        user.setId(2L);
        user.setUsername("user1");
        user.setNickname("测试用户");
        when(sysUserMapper.selectBatchIds(any())).thenReturn(java.util.List.of(user));

        AdminRegistrationPageQuery query = new AdminRegistrationPageQuery();
        var result = registrationService.pageByActivity(1L, query);

        assertEquals(1L, result.getActivity().getId());
        assertEquals("测试活动", result.getActivity().getTitle());
        assertEquals(1, result.getList().size());
        assertEquals("user1", result.getList().get(0).getUsername());
        assertEquals("测试用户", result.getList().get(0).getNickname());
    }

    private SysUser registrantUser() {
        SysUser user = new SysUser();
        user.setId(2L);
        user.setUsername("user1");
        user.setProvinceName("广东省");
        user.setCityName("深圳市");
        user.setDistrictName("南山区");
        user.setSchoolName("实验小学");
        return user;
    }

    private ActivityRegistration pendingRegistration(Long id, Long userId, Long activityId) {
        ActivityRegistration registration = new ActivityRegistration();
        registration.setId(id);
        registration.setUserId(userId);
        registration.setActivityId(activityId);
        registration.setStatus(RegistrationStatus.PENDING);
        registration.setApplyTime(LocalDateTime.now());
        return registration;
    }
}
