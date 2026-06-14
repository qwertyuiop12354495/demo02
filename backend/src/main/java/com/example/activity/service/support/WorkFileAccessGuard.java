package com.example.activity.service.support;

import com.example.activity.common.auth.AuthUser;
import com.example.activity.common.enums.RoleTypeEnum;
import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;
import com.example.activity.common.scope.ScopeNameMatcher;
import com.example.activity.entity.Work;
import org.springframework.stereotype.Component;

@Component
public class WorkFileAccessGuard {

    public void requireListAccess(AuthUser user, Work work) {
        if (user == null || work == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (user.getRoleType() == RoleTypeEnum.TEACHER) {
            ScopeNameMatcher.requireTeacherOwner(user, work.getTeacherId());
            return;
        }
        if (user.getRoleType().isReviewerRole() || user.getRoleType().isAdminRole()) {
            ScopeNameMatcher.requireScopeConfigured(user);
            ScopeNameMatcher.requireAccess(user, work);
            return;
        }
        throw new BusinessException(ErrorCode.FORBIDDEN);
    }
}
