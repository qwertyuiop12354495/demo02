package com.example.activity.service.support;

import com.example.activity.common.auth.AuthContext;
import com.example.activity.common.auth.AuthUser;
import com.example.activity.common.auth.RoleGuard;
import com.example.activity.common.enums.RoleTypeEnum;
import com.example.activity.common.enums.WorkStatusEnum;
import com.example.activity.common.enums.WorkStepEnum;
import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;
import com.example.activity.entity.Work;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class ScoreAccessGuard {

    private static final Set<RoleTypeEnum> SCORER_ROLES = Set.of(
            RoleTypeEnum.DISTRICT_REVIEWER,
            RoleTypeEnum.CITY_REVIEWER,
            RoleTypeEnum.PROVINCE_REVIEWER
    );

    private static final Map<RoleTypeEnum, WorkStepEnum> ROLE_SCORE_STEP = Map.of(
            RoleTypeEnum.DISTRICT_REVIEWER, WorkStepEnum.SCORE_DISTRICT,
            RoleTypeEnum.CITY_REVIEWER, WorkStepEnum.SCORE_CITY,
            RoleTypeEnum.PROVINCE_REVIEWER, WorkStepEnum.SCORE_PROVINCE
    );

    public AuthUser requireScorer() {
        RoleGuard.requireAnyRole(SCORER_ROLES.toArray(RoleTypeEnum[]::new));
        return AuthContext.require();
    }

    public WorkStepEnum resolveScoreStep(RoleTypeEnum roleType) {
        WorkStepEnum step = ROLE_SCORE_STEP.get(roleType);
        if (step == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return step;
    }

    public void requireStepMatchesRole(AuthUser user, Work work) {
        WorkStepEnum expected = resolveScoreStep(user.getRoleType());
        if (work.getCurrentStep() != expected) {
            throw new BusinessException(ErrorCode.WORK_NOT_SCORABLE, "作品当前打分级别不匹配");
        }
    }

    public void requireScorableStatus(Work work) {
        if (work.getCurrentStatus() != WorkStatusEnum.SUBMITTED) {
            throw new BusinessException(ErrorCode.WORK_NOT_SCORABLE);
        }
    }
}
