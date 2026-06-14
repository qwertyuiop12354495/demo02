package com.example.activity.service.support;

import com.example.activity.common.enums.ActivityStatus;
import com.example.activity.common.enums.WorkStatusEnum;
import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;
import com.example.activity.entity.Activity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ActivityEnrollmentValidatorTest {

    private ActivityEnrollmentValidator validator;
    private Activity activity;

    @BeforeEach
    void setUp() {
        validator = new ActivityEnrollmentValidator();
        activity = new Activity();
        activity.setId(1L);
        activity.setStatus(ActivityStatus.PUBLISHED);
        activity.setSignupStartTime(LocalDateTime.now().minusHours(1));
        activity.setSignupEndTime(LocalDateTime.now().plusDays(1));
    }

    @Test
    void requirePublishedAndInSignupPeriod_shouldPassWhenValid() {
        validator.requirePublishedAndInSignupPeriod(activity, LocalDateTime.now());
    }

    @Test
    void requirePublishedAndInSignupPeriod_shouldRejectWhenOffline() {
        activity.setStatus(ActivityStatus.OFFLINE);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.requirePublishedAndInSignupPeriod(activity, LocalDateTime.now()));
        assertEquals(ErrorCode.ACTIVITY_OFFLINE.getCode(), ex.getCode());
    }

    @Test
    void requirePublishedAndInSignupPeriod_shouldRejectWhenNotStarted() {
        activity.setSignupStartTime(LocalDateTime.now().plusHours(1));
        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.requirePublishedAndInSignupPeriod(activity, LocalDateTime.now()));
        assertEquals(ErrorCode.REGISTRATION_NOT_STARTED.getCode(), ex.getCode());
    }

    @Test
    void requireBeforeUploadDeadline_shouldUseSignupEndWhenUploadDeadlineNull() {
        LocalDateTime deadline = validator.resolveUploadDeadline(activity);
        assertEquals(activity.getSignupEndTime(), deadline);
        validator.requireBeforeUploadDeadline(activity, LocalDateTime.now());
    }

    @Test
    void requireBeforeUploadDeadline_shouldRejectWhenPassed() {
        activity.setUploadDeadline(LocalDateTime.now().minusMinutes(1));
        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.requireBeforeUploadDeadline(activity, LocalDateTime.now()));
        assertEquals(ErrorCode.UPLOAD_DEADLINE_PASSED.getCode(), ex.getCode());
    }

    @Test
    void requireAllowTeacherEdit_shouldRejectDraftAfterSignupEnd() {
        activity.setSignupEndTime(LocalDateTime.now().minusMinutes(1));
        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.requireAllowTeacherEdit(activity, WorkStatusEnum.DRAFT, LocalDateTime.now()));
        assertEquals(ErrorCode.REGISTRATION_CLOSED.getCode(), ex.getCode());
    }

    @Test
    void requireAllowTeacherSubmit_shouldAllowRevisionBeforeUploadDeadline() {
        validator.requireAllowTeacherSubmit(activity, WorkStatusEnum.REVISION_REQUIRED, LocalDateTime.now());
    }
}
