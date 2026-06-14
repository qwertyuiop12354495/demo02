package com.example.activity.service.support;

import com.example.activity.common.auth.AuthUser;
import com.example.activity.common.enums.WorkStatusEnum;
import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;
import com.example.activity.common.scope.ScopeNameMatcher;
import com.example.activity.entity.Activity;
import com.example.activity.entity.Work;
import com.example.activity.mapper.ActivityMapper;
import com.example.activity.mapper.WorkMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 教师端作品读写：本人校验 + 可编辑状态 + 活动报名时间窗。
 */
@Component
@RequiredArgsConstructor
public class WorkTeacherAccessSupport {

    private final WorkMapper workMapper;
    private final ActivityMapper activityMapper;
    private final ActivityEnrollmentValidator activityEnrollmentValidator;

    public Work requireOwnedWork(Long workId, AuthUser user) {
        Work work = workMapper.selectById(workId);
        if (work == null || Objects.equals(work.getDeleted(), 1)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "作品不存在");
        }
        ScopeNameMatcher.requireTeacherOwner(user, work.getTeacherId());
        return work;
    }

    public Work requireOwnedEditableWork(Long workId, AuthUser user) {
        Work work = requireOwnedWork(workId, user);
        if (!isEditableStatus(work.getCurrentStatus())) {
            throw new BusinessException(ErrorCode.WORK_NOT_EDITABLE);
        }
        return work;
    }

    public Work requireOwnedEditableWorkInOpenWindow(Long workId, AuthUser user) {
        Work work = requireOwnedEditableWork(workId, user);
        Activity activity = requireActivity(work.getActivityId());
        activityEnrollmentValidator.requireAllowTeacherEdit(
                activity, work.getCurrentStatus(), LocalDateTime.now());
        return work;
    }

    public Activity requireActivityForSubmit(Work work) {
        Activity activity = requireActivity(work.getActivityId());
        activityEnrollmentValidator.requireAllowTeacherSubmit(
                activity, work.getCurrentStatus(), LocalDateTime.now());
        return activity;
    }

    public Activity requireActivity(Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "活动不存在");
        }
        return activity;
    }

    public static boolean isEditableStatus(WorkStatusEnum status) {
        return status == WorkStatusEnum.DRAFT || status == WorkStatusEnum.REVISION_REQUIRED;
    }
}
