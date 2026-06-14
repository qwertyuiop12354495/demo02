package com.example.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.activity.common.enums.ActivityStatus;
import com.example.activity.common.enums.RegistrationStatus;
import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;
import com.example.activity.common.result.PageResult;
import com.example.activity.converter.ActivityConverter;
import com.example.activity.dto.query.ActivityPageQuery;
import com.example.activity.dto.query.AdminActivityPageQuery;
import com.example.activity.dto.request.activity.ActivityCreateRequest;
import com.example.activity.dto.request.activity.ActivityStatusUpdateRequest;
import com.example.activity.dto.request.activity.ActivityUpdateRequest;
import com.example.activity.entity.Activity;
import com.example.activity.entity.ActivityRegistration;
import com.example.activity.mapper.ActivityMapper;
import com.example.activity.mapper.ActivityRegistrationMapper;
import com.example.activity.common.auth.AuthUser;
import com.example.activity.service.ActivityService;
import com.example.activity.service.support.ActivityAdminAccessGuard;
import com.example.activity.vo.activity.ActivityStatusUpdateVO;
import com.example.activity.vo.activity.ActivityVO;
import com.example.activity.vo.activity.AdminActivityListItemVO;
import com.example.activity.vo.activity.UserActivityListItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityMapper activityMapper;
    private final ActivityRegistrationMapper activityRegistrationMapper;
    private final ActivityConverter activityConverter;
    private final ActivityAdminAccessGuard activityAdminAccessGuard;

    @Override
    public PageResult<UserActivityListItemVO> pagePublishedActivities(ActivityPageQuery query, Long userId) {
        LambdaQueryWrapper<Activity> wrapper = buildCommonQueryWrapper(query);
        wrapper.eq(Activity::getStatus, ActivityStatus.PUBLISHED)
                .orderByAsc(Activity::getSignupEndTime);

        Page<Activity> page = activityMapper.selectPage(new Page<>(query.getPage(), query.getPageSize()), wrapper);
        List<UserActivityListItemVO> list = page.getRecords().stream()
                .map(activity -> enrichUserListItem(activity, userId))
                .toList();
        return toPageResult(list, page);
    }

    @Override
    public PageResult<AdminActivityListItemVO> pageAdminActivities(AdminActivityPageQuery query) {
        AuthUser admin = activityAdminAccessGuard.requireProvinceAdmin();
        LambdaQueryWrapper<Activity> wrapper = buildCommonQueryWrapper(query);
        if (query.getStatus() != null) {
            wrapper.eq(Activity::getStatus, query.getStatus());
        }
        activityAdminAccessGuard.applyProvinceScope(wrapper, admin);
        wrapper.orderByDesc(Activity::getUpdatedAt);

        Page<Activity> page = activityMapper.selectPage(new Page<>(query.getPage(), query.getPageSize()), wrapper);
        List<AdminActivityListItemVO> list = page.getRecords().stream()
                .map(activityConverter::toAdminListItemVO)
                .toList();
        return toPageResult(list, page);
    }

    @Override
    public ActivityVO getPublishedActivityDetail(Long id, Long userId) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null || activity.getStatus() != ActivityStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "活动不存在");
        }

        ActivityVO vo = activityConverter.toVO(activity);
        ActivityRegistration registration = findUserRegistration(userId, id);
        vo.setMyRegistration(activityConverter.toRegistrationBriefVO(registration));
        fillRegisterInfo(vo, activity, registration);
        return vo;
    }

    @Override
    public ActivityVO createActivity(ActivityCreateRequest request, Long adminId) {
        activityAdminAccessGuard.requireProvinceAdmin();
        if (adminId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        validateActivityTimes(request.getRegistrationStartTime(), request.getRegistrationDeadline(),
                request.getEventStartTime(), request.getEventEndTime());
        validateMaxParticipants(request.getMaxParticipants(), 0);

        Activity activity = activityConverter.fromCreateRequest(request, adminId);
        activity.setStatus(ActivityStatus.DRAFT);
        activityMapper.insert(activity);
        return activityConverter.toVO(activity);
    }

    @Override
    public ActivityVO updateActivity(Long id, ActivityUpdateRequest request) {
        AuthUser admin = activityAdminAccessGuard.requireProvinceAdmin();
        Activity activity = requireActivity(id);
        activityAdminAccessGuard.requireActivityInProvince(activity, admin);
        validateActivityTimes(request.getRegistrationStartTime(), request.getRegistrationDeadline(),
                request.getEventStartTime(), request.getEventEndTime());
        validateMaxParticipants(request.getMaxParticipants(), activity.getCurrentCount());

        activityConverter.updateFromRequest(activity, request);
        activityMapper.updateById(activity);
        return activityConverter.toVO(activity);
    }

    @Override
    public ActivityStatusUpdateVO updateActivityStatus(Long id, ActivityStatusUpdateRequest request) {
        AuthUser admin = activityAdminAccessGuard.requireProvinceAdmin();
        Activity activity = requireActivity(id);
        activityAdminAccessGuard.requireActivityInProvince(activity, admin);
        ActivityStatus targetStatus = request.getStatus().toActivityStatus();
        validateStatusTransition(activity.getStatus(), targetStatus);

        activity.setStatus(targetStatus);
        activityMapper.updateById(activity);
        return activityConverter.toStatusUpdateVO(activity);
    }

    private LambdaQueryWrapper<Activity> buildCommonQueryWrapper(ActivityPageQuery query) {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(Activity::getTitle, query.getKeyword().trim());
        }
        if (query.getEventStartFrom() != null) {
            wrapper.ge(Activity::getStartTime, query.getEventStartFrom());
        }
        if (query.getEventStartTo() != null) {
            wrapper.le(Activity::getStartTime, query.getEventStartTo());
        }
        return wrapper;
    }

    private UserActivityListItemVO enrichUserListItem(Activity activity, Long userId) {
        UserActivityListItemVO vo = activityConverter.toUserListItemVO(activity);
        ActivityRegistration registration = findUserRegistration(userId, activity.getId());
        fillRegisterInfo(vo, activity, registration);
        return vo;
    }

    private void fillRegisterInfo(UserActivityListItemVO vo, Activity activity, ActivityRegistration registration) {
        RegisterCheckResult check = evaluateCanRegister(activity, registration);
        vo.setCanRegister(check.canRegister());
        vo.setRegisterDisabledReason(check.reason());
    }

    private void fillRegisterInfo(ActivityVO vo, Activity activity, ActivityRegistration registration) {
        RegisterCheckResult check = evaluateCanRegister(activity, registration);
        vo.setCanRegister(check.canRegister());
        vo.setRegisterDisabledReason(check.reason());
    }

    private RegisterCheckResult evaluateCanRegister(Activity activity, ActivityRegistration registration) {
        LocalDateTime signupStart = activity.getSignupStartTime();
        LocalDateTime signupEnd = activity.getSignupEndTime();
        Integer currentCount = activity.getCurrentCount();
        Integer maxCount = activity.getMaxCount();
        if (signupStart == null || signupEnd == null || currentCount == null || maxCount == null) {
            return new RegisterCheckResult(false, "活动信息不完整");
        }

        LocalDateTime now = LocalDateTime.now();
        if (registration != null) {
            RegistrationStatus status = registration.getStatus();
            if (status == RegistrationStatus.PENDING || status == RegistrationStatus.APPROVED) {
                return new RegisterCheckResult(false, "已报名");
            }
            if (status == RegistrationStatus.REJECTED) {
                return new RegisterCheckResult(false, "报名已被拒绝");
            }
            if (status == RegistrationStatus.CANCELLED) {
                return new RegisterCheckResult(false, "报名已取消");
            }
        }
        if (activity.getStatus() != ActivityStatus.PUBLISHED) {
            return new RegisterCheckResult(false, "活动未上架");
        }
        if (now.isBefore(signupStart)) {
            return new RegisterCheckResult(false, "报名尚未开始");
        }
        if (now.isAfter(signupEnd)) {
            return new RegisterCheckResult(false, "活动报名已截止");
        }
        if (currentCount >= maxCount) {
            return new RegisterCheckResult(false, "活动名额已满");
        }
        return new RegisterCheckResult(true, null);
    }

    private ActivityRegistration findUserRegistration(Long userId, Long activityId) {
        if (userId == null) {
            return null;
        }
        return activityRegistrationMapper.selectOne(new LambdaQueryWrapper<ActivityRegistration>()
                .eq(ActivityRegistration::getUserId, userId)
                .eq(ActivityRegistration::getActivityId, activityId));
    }

    private Activity requireActivity(Long id) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "活动不存在");
        }
        return activity;
    }

    private void validateActivityTimes(LocalDateTime registrationStartTime, LocalDateTime registrationDeadline,
                                       LocalDateTime eventStartTime, LocalDateTime eventEndTime) {
        if (registrationStartTime == null || registrationDeadline == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "报名时间不能为空");
        }
        if (!registrationStartTime.isBefore(registrationDeadline)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "报名开始时间必须早于报名截止时间");
        }
        if (eventStartTime != null && eventEndTime != null && eventStartTime.isAfter(eventEndTime)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "活动开始时间不能晚于活动结束时间");
        }
    }

    private void validateMaxParticipants(Integer maxParticipants, Integer approvedCount) {
        if (maxParticipants == null || maxParticipants < 1) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "人数上限不能小于1");
        }
        int approved = approvedCount == null ? 0 : approvedCount;
        if (maxParticipants < approved) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "人数上限不能小于已通过人数");
        }
    }

    private void validateStatusTransition(ActivityStatus current, ActivityStatus target) {
        if (current == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "活动状态异常");
        }
        boolean allowed = switch (current) {
            case DRAFT -> target == ActivityStatus.PUBLISHED;
            case PUBLISHED -> target == ActivityStatus.OFFLINE;
            case OFFLINE -> target == ActivityStatus.PUBLISHED;
        };
        if (!allowed) {
            throw new BusinessException(ErrorCode.INVALID_STATUS, "不允许的状态变更");
        }
    }

    private <T> PageResult<T> toPageResult(List<T> list, Page<Activity> page) {
        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize());
    }

    private record RegisterCheckResult(boolean canRegister, String reason) {
    }
}
