package com.example.activity.service.support;

import com.example.activity.common.auth.AuthContext;
import com.example.activity.common.auth.AuthUser;
import com.example.activity.common.enums.RoleTypeEnum;
import com.example.activity.common.enums.WorkStatusEnum;
import com.example.activity.common.enums.WorkStepEnum;
import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;
import com.example.activity.entity.Work;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkReviewAccessGuardTest {

    private WorkReviewAccessGuard guard;

    @BeforeEach
    void setUp() {
        guard = new WorkReviewAccessGuard();
    }

    @AfterEach
    void tearDown() {
        AuthContext.clear();
    }

    @Test
    void resolveReviewStep_shouldMapSchoolAdminToSchool() {
        assertEquals(WorkStepEnum.SCHOOL, guard.resolveReviewStep(RoleTypeEnum.SCHOOL_ADMIN));
        assertEquals(WorkStepEnum.PROVINCE, guard.resolveReviewStep(RoleTypeEnum.PROVINCE_ADMIN));
    }

    @Test
    void resolveNextStepOnApprove_shouldAdvanceReviewChain() {
        assertEquals(WorkStepEnum.DISTRICT, guard.resolveNextStepOnApprove(WorkStepEnum.SCHOOL));
        assertEquals(WorkStepEnum.SCORE_DISTRICT, guard.resolveNextStepOnApprove(WorkStepEnum.PROVINCE));
    }

    @Test
    void requireStepMatchesRole_shouldRejectMismatch() {
        AuthUser user = AuthUser.of(1L, "schoolAdmin", RoleTypeEnum.SCHOOL_ADMIN,
                "广东省", "深圳市", "南山区", "实验小学");
        Work work = new Work();
        work.setCurrentStep(WorkStepEnum.DISTRICT);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> guard.requireStepMatchesRole(user, work));
        assertEquals(ErrorCode.REVIEW_STEP_MISMATCH.getCode(), ex.getCode());
    }

    @Test
    void requireReviewableStatus_shouldRejectNonSubmitted() {
        Work work = new Work();
        work.setCurrentStatus(WorkStatusEnum.REVISION_REQUIRED);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> guard.requireReviewableStatus(work));
        assertEquals(ErrorCode.WORK_NOT_REVIEWABLE.getCode(), ex.getCode());
    }
}
