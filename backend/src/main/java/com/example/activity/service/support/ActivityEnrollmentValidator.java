package com.example.activity.service.support;

import com.example.activity.common.enums.ActivityStatus;
import com.example.activity.common.enums.WorkStatusEnum;
import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;
import com.example.activity.entity.Activity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ActivityEnrollmentValidator {

    public void requirePublished(Activity activity) {
        if (activity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "活动不存在");
        }
        if (activity.getStatus() != ActivityStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.ACTIVITY_OFFLINE);
        }
    }

    public void requirePublishedAndInSignupPeriod(Activity activity, LocalDateTime now) {
        requirePublished(activity);
        requireInSignupPeriod(activity, now);
    }

    public void requireBeforeUploadDeadline(Activity activity, LocalDateTime now) {
        if (activity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "活动不存在");
        }
        LocalDateTime deadline = resolveUploadDeadline(activity);
        if (now.isAfter(deadline)) {
            throw new BusinessException(ErrorCode.UPLOAD_DEADLINE_PASSED);
        }
    }

    /**
     * 教师保存/上传/删除材料：草稿须在报名期内；退回修改须在材料截止前。
     */
    public void requireAllowTeacherEdit(Activity activity, WorkStatusEnum status, LocalDateTime now) {
        requirePublished(activity);
        if (status == WorkStatusEnum.DRAFT) {
            requireInSignupPeriod(activity, now);
        } else if (status == WorkStatusEnum.REVISION_REQUIRED) {
            requireBeforeUploadDeadline(activity, now);
        } else {
            throw new BusinessException(ErrorCode.WORK_NOT_EDITABLE);
        }
    }

    /**
     * 教师提交报名：首次提交须在报名期内；退回后再提交须在材料截止前。
     */
    public void requireAllowTeacherSubmit(Activity activity, WorkStatusEnum status, LocalDateTime now) {
        if (status == WorkStatusEnum.DRAFT) {
            requirePublishedAndInSignupPeriod(activity, now);
        } else if (status == WorkStatusEnum.REVISION_REQUIRED) {
            requirePublished(activity);
            requireBeforeUploadDeadline(activity, now);
        } else {
            throw new BusinessException(ErrorCode.WORK_NOT_EDITABLE);
        }
    }

    private void requireInSignupPeriod(Activity activity, LocalDateTime now) {
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

    public LocalDateTime resolveUploadDeadline(Activity activity) {
        LocalDateTime uploadDeadline = activity.getUploadDeadline();
        if (uploadDeadline != null) {
            return uploadDeadline;
        }
        LocalDateTime signupEnd = activity.getSignupEndTime();
        if (signupEnd == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "活动报名截止时间配置异常");
        }
        return signupEnd;
    }
}
