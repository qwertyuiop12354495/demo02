package com.example.activity.service.support;

import com.example.activity.common.auth.AuthUser;
import com.example.activity.common.auth.RoleGuard;
import com.example.activity.common.enums.RoleTypeEnum;
import com.example.activity.common.enums.WorkStatusEnum;
import com.example.activity.common.enums.WorkStepEnum;
import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;
import com.example.activity.entity.Work;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

@Component
public class WorkReviewAccessGuard {

    private static final Set<RoleTypeEnum> REVIEW_ADMIN_ROLES = Set.of(
            RoleTypeEnum.SCHOOL_ADMIN,
            RoleTypeEnum.DISTRICT_ADMIN,
            RoleTypeEnum.CITY_ADMIN,
            RoleTypeEnum.PROVINCE_ADMIN
    );

    private static final Map<RoleTypeEnum, WorkStepEnum> ROLE_REVIEW_STEP = Map.of(
            RoleTypeEnum.SCHOOL_ADMIN, WorkStepEnum.SCHOOL,
            RoleTypeEnum.DISTRICT_ADMIN, WorkStepEnum.DISTRICT,
            RoleTypeEnum.CITY_ADMIN, WorkStepEnum.CITY,
            RoleTypeEnum.PROVINCE_ADMIN, WorkStepEnum.PROVINCE
    );

    private static final Map<WorkStepEnum, WorkStepEnum> APPROVE_NEXT_STEP = new EnumMap<>(WorkStepEnum.class);

    static {
        APPROVE_NEXT_STEP.put(WorkStepEnum.SCHOOL, WorkStepEnum.DISTRICT);
        APPROVE_NEXT_STEP.put(WorkStepEnum.DISTRICT, WorkStepEnum.CITY);
        APPROVE_NEXT_STEP.put(WorkStepEnum.CITY, WorkStepEnum.PROVINCE);
        APPROVE_NEXT_STEP.put(WorkStepEnum.PROVINCE, WorkStepEnum.SCORE_DISTRICT);
    }

    public AuthUser requireReviewAdmin() {
        RoleGuard.requireAnyRole(REVIEW_ADMIN_ROLES.toArray(RoleTypeEnum[]::new));
        return com.example.activity.common.auth.AuthContext.require();
    }

    public WorkStepEnum resolveReviewStep(RoleTypeEnum roleType) {
        WorkStepEnum step = ROLE_REVIEW_STEP.get(roleType);
        if (step == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return step;
    }

    public void requireStepMatchesRole(AuthUser user, Work work) {
        WorkStepEnum expectedStep = resolveReviewStep(user.getRoleType());
        if (work.getCurrentStep() != expectedStep) {
            throw new BusinessException(ErrorCode.REVIEW_STEP_MISMATCH);
        }
    }

    public void requireReviewableStatus(Work work) {
        if (work.getCurrentStatus() != WorkStatusEnum.SUBMITTED) {
            throw new BusinessException(ErrorCode.WORK_NOT_REVIEWABLE);
        }
    }

    public WorkStepEnum resolveNextStepOnApprove(WorkStepEnum currentStep) {
        WorkStepEnum nextStep = APPROVE_NEXT_STEP.get(currentStep);
        if (nextStep == null) {
            throw new BusinessException(ErrorCode.REVIEW_STEP_MISMATCH);
        }
        return nextStep;
    }
}
