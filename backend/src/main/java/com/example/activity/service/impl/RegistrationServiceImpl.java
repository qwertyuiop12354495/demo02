package com.example.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.activity.common.auth.AuthContext;
import com.example.activity.common.auth.AuthUser;
import com.example.activity.common.auth.RoleGuard;
import com.example.activity.common.enums.ActivityStatus;
import com.example.activity.common.enums.RegistrationAuditAction;
import com.example.activity.common.enums.RegistrationStatus;
import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;
import com.example.activity.common.result.PageResult;
import com.example.activity.converter.RegistrationConverter;
import com.example.activity.dto.query.AdminRegistrationPageQuery;
import com.example.activity.dto.query.RegistrationMinePageQuery;
import com.example.activity.dto.request.registration.RegistrationAuditRequest;
import com.example.activity.dto.request.registration.RegistrationCreateRequest;
import com.example.activity.entity.Activity;
import com.example.activity.entity.ActivityRegistration;
import com.example.activity.entity.SysUser;
import com.example.activity.mapper.ActivityMapper;
import com.example.activity.mapper.ActivityRegistrationMapper;
import com.example.activity.mapper.SysUserMapper;
import com.example.activity.service.ActivityQuotaService;
import com.example.activity.service.RegistrationService;
import com.example.activity.service.support.ActivityAdminAccessGuard;
import com.example.activity.service.support.RegistrationScopeQueryBuilder;
import com.example.activity.vo.registration.RegistrationAdminListVO;
import com.example.activity.vo.registration.RegistrationCancelVO;
import com.example.activity.vo.registration.RegistrationListItemVO;
import com.example.activity.vo.registration.RegistrationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final ActivityMapper activityMapper;
    private final ActivityRegistrationMapper activityRegistrationMapper;
    private final SysUserMapper sysUserMapper;
    private final ActivityQuotaService activityQuotaService;
    private final RegistrationConverter registrationConverter;
    private final ActivityAdminAccessGuard activityAdminAccessGuard;
    private final RegistrationScopeQueryBuilder registrationScopeQueryBuilder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegistrationVO register(RegistrationCreateRequest request, Long userId) {
        requireUserId(userId);

        Long activityId = request.getActivityId();

        // 1. 行级锁 + 业务校验 + 重复报名检查
        Activity activity = activityQuotaService.lockActivity(activityId);
        validateActivityForRegistration(activity);
        ensureNotRegistered(userId, activityId);

        // 2. 条件更新原子预占名额（核心并发控制，不依赖内存中的 current_count 判断）
        Activity reservedActivity = activityQuotaService.reserveQuota(activityId);

        // 3. 占位成功后再插入报名记录；失败则整个事务回滚（含名额回退）
        ActivityRegistration registration = buildPendingRegistration(request, userId);
        insertRegistration(registration);

        return registrationConverter.toVO(registration, reservedActivity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegistrationCancelVO cancel(Long registrationId, Long userId) {
        requireUserId(userId);

        // 先锁报名记录，再锁活动行，避免并发重复取消
        ActivityRegistration registration = activityRegistrationMapper.selectByIdForUpdate(registrationId);
        if (registration == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "报名记录不存在");
        }
        if (!Objects.equals(registration.getUserId(), userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该报名记录");
        }

        RegistrationStatus status = registration.getStatus();
        if (status != RegistrationStatus.PENDING && status != RegistrationStatus.APPROVED) {
            throw new BusinessException(ErrorCode.INVALID_STATUS, "当前状态不允许取消报名");
        }

        activityQuotaService.lockActivity(registration.getActivityId());

        registration.setStatus(RegistrationStatus.CANCELLED);
        activityRegistrationMapper.updateById(registration);

        activityQuotaService.releaseQuota(registration.getActivityId());

        return registrationConverter.toCancelVO(registration);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<RegistrationListItemVO> pageMine(RegistrationMinePageQuery query, Long userId) {
        requireUserId(userId);

        LambdaQueryWrapper<ActivityRegistration> wrapper = new LambdaQueryWrapper<ActivityRegistration>()
                .eq(ActivityRegistration::getUserId, userId)
                .eq(query.getStatus() != null, ActivityRegistration::getStatus, query.getStatus())
                .orderByDesc(ActivityRegistration::getApplyTime);

        Page<ActivityRegistration> page = activityRegistrationMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()), wrapper);

        Map<Long, Activity> activityMap = loadActivities(page.getRecords());
        List<RegistrationListItemVO> list = page.getRecords().stream()
                .map(registration -> registrationConverter.toListItemVO(
                        registration, activityMap.get(registration.getActivityId())))
                .toList();

        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public RegistrationAdminListVO pageByActivity(Long activityId, AdminRegistrationPageQuery query) {
        AuthUser admin = activityAdminAccessGuard.requireProvinceAdmin();
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "活动不存在");
        }
        activityAdminAccessGuard.requireActivityInProvince(activity, admin);

        LambdaQueryWrapper<ActivityRegistration> wrapper = new LambdaQueryWrapper<ActivityRegistration>()
                .eq(ActivityRegistration::getActivityId, activityId)
                .eq(query.getStatus() != null, ActivityRegistration::getStatus, query.getStatus())
                .orderByDesc(ActivityRegistration::getApplyTime);
        registrationScopeQueryBuilder.applyRegistrantScope(wrapper, admin);

        Page<ActivityRegistration> page = activityRegistrationMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()), wrapper);

        Map<Long, SysUser> userMap = loadUsers(page.getRecords());
        List<RegistrationListItemVO> list = page.getRecords().stream()
                .map(registration -> registrationConverter.toAdminListItemVO(
                        registration, userMap.get(registration.getUserId())))
                .toList();

        RegistrationAdminListVO result = new RegistrationAdminListVO();
        result.setActivity(buildActivitySummary(activity, activityId));
        result.setList(list);
        result.setTotal(page.getTotal());
        result.setPage(page.getCurrent());
        result.setPageSize(page.getSize());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegistrationVO audit(Long registrationId, RegistrationAuditRequest request, Long adminId) {
        requireUserId(adminId);
        AuthUser admin = AuthContext.require();
        RoleGuard.requireLegacyAdmin();

        RegistrationAuditAction action = RegistrationAuditAction.from(request.getAction());

        ActivityRegistration registration = activityRegistrationMapper.selectByIdForUpdate(registrationId);
        if (registration == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "报名记录不存在");
        }
        if (registration.getStatus() != RegistrationStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_STATUS, "仅待审核状态的报名可审核");
        }

        SysUser registrant = requireRegistrant(registration.getUserId());
        registrationScopeQueryBuilder.requireRegistrantAccess(admin, registrant);

        activityQuotaService.lockActivity(registration.getActivityId());

        LocalDateTime now = LocalDateTime.now();
        registration.setAuditTime(now);
        registration.setAuditRemark(request.getAuditRemark());
        registration.setAuditedBy(adminId);

        if (action == RegistrationAuditAction.APPROVE) {
            registration.setStatus(RegistrationStatus.APPROVED);
        } else {
            registration.setStatus(RegistrationStatus.REJECTED);
            activityQuotaService.releaseQuota(registration.getActivityId());
        }

        activityRegistrationMapper.updateById(registration);
        return registrationConverter.toVO(registration, null);
    }

    private RegistrationAdminListVO.ActivitySummaryVO buildActivitySummary(Activity activity, Long activityId) {
        long approvedCount = activityRegistrationMapper.selectCount(
                new LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getActivityId, activityId)
                        .eq(ActivityRegistration::getStatus, RegistrationStatus.APPROVED));
        long pendingCount = activityRegistrationMapper.selectCount(
                new LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getActivityId, activityId)
                        .eq(ActivityRegistration::getStatus, RegistrationStatus.PENDING));

        RegistrationAdminListVO.ActivitySummaryVO summary = new RegistrationAdminListVO.ActivitySummaryVO();
        summary.setId(activity.getId());
        summary.setTitle(activity.getTitle());
        summary.setMaxParticipants(activity.getMaxCount());
        summary.setApprovedCount((int) approvedCount);
        summary.setPendingCount((int) pendingCount);
        return summary;
    }

    private Map<Long, SysUser> loadUsers(List<ActivityRegistration> registrations) {
        Set<Long> userIds = registrations.stream()
                .map(ActivityRegistration::getUserId)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return sysUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity()));
    }

    private void validateActivityForRegistration(Activity activity) {
        if (activity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "活动不存在");
        }
        if (activity.getStatus() != ActivityStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.ACTIVITY_OFFLINE);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime signupStart = activity.getSignupStartTime();
        LocalDateTime signupEnd = activity.getSignupEndTime();
        if (signupStart == null || signupEnd == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "活动报名时间配置异常");
        }
        if (now.isBefore(signupStart)) {
            throw new BusinessException(ErrorCode.REGISTRATION_NOT_STARTED);
        }
        if (now.isAfter(signupEnd)) {
            throw new BusinessException(ErrorCode.REGISTRATION_CLOSED);
        }
    }

    private void ensureNotRegistered(Long userId, Long activityId) {
        ActivityRegistration existing = activityRegistrationMapper.selectOne(
                new LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getUserId, userId)
                        .eq(ActivityRegistration::getActivityId, activityId));
        if (existing != null) {
            throw new BusinessException(ErrorCode.DUPLICATE_REGISTRATION);
        }
    }

    private ActivityRegistration buildPendingRegistration(RegistrationCreateRequest request, Long userId) {
        ActivityRegistration registration = new ActivityRegistration();
        registration.setUserId(userId);
        registration.setActivityId(request.getActivityId());
        registration.setStatus(RegistrationStatus.PENDING);
        registration.setApplyTime(LocalDateTime.now());
        registration.setApplyRemark(request.getRemark());
        return registration;
    }

    private void insertRegistration(ActivityRegistration registration) {
        try {
            activityRegistrationMapper.insert(registration);
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(ErrorCode.DUPLICATE_REGISTRATION);
        }
    }

    private Map<Long, Activity> loadActivities(List<ActivityRegistration> registrations) {
        Set<Long> activityIds = registrations.stream()
                .map(ActivityRegistration::getActivityId)
                .collect(Collectors.toSet());
        if (activityIds.isEmpty()) {
            return Map.of();
        }
        return activityMapper.selectBatchIds(activityIds).stream()
                .collect(Collectors.toMap(Activity::getId, Function.identity()));
    }

    private SysUser requireRegistrant(Long userId) {
        SysUser registrant = sysUserMapper.selectById(userId);
        if (registrant == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "报名用户不存在");
        }
        return registrant;
    }

    private void requireUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }
}
