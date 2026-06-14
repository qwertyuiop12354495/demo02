package com.example.activity.common.scope;

import com.example.activity.common.auth.AuthUser;
import com.example.activity.common.enums.RoleTypeEnum;
import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;

public final class ScopeNameMatcher {

    private ScopeNameMatcher() {
    }

    public static boolean canAccess(AuthUser user, ScopedData data) {
        return canAccess(user, data, null);
    }

    public static boolean canAccess(AuthUser user, ScopedData data, Long ownerUserId) {
        if (user == null || data == null) {
            return false;
        }
        UserScope userScope = UserScope.fromAuthUser(user);
        if (!isScopeConfigured(user.getRoleType(), userScope)) {
            return false;
        }
        return matchesByRole(user, userScope, data, ownerUserId);
    }

    public static void requireAccess(AuthUser user, ScopedData data) {
        requireAccess(user, data, null);
    }

    public static void requireAccess(AuthUser user, ScopedData data, Long ownerUserId) {
        if (!canAccess(user, data, ownerUserId)) {
            throw new BusinessException(ErrorCode.SCOPE_ACCESS_DENIED);
        }
    }

    public static void requireScopeConfigured(AuthUser user) {
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (!isScopeConfigured(user.getRoleType(), UserScope.fromAuthUser(user))) {
            throw new BusinessException(ErrorCode.SCOPE_NOT_CONFIGURED);
        }
    }

    static boolean matchesByRole(AuthUser user, UserScope userScope, ScopedData data, Long ownerUserId) {
        return switch (user.getRoleType()) {
            case TEACHER -> matchesTeacher(user, userScope, data, ownerUserId);
            case SCHOOL_ADMIN -> matchesSchool(userScope, data);
            case DISTRICT_ADMIN, DISTRICT_REVIEWER -> matchesDistrict(userScope, data);
            case CITY_ADMIN, CITY_REVIEWER -> matchesCity(userScope, data);
            case PROVINCE_ADMIN, PROVINCE_REVIEWER -> matchesProvince(userScope, data);
        };
    }

    static boolean isScopeConfigured(RoleTypeEnum roleType, UserScope scope) {
        return switch (roleType) {
            case TEACHER, SCHOOL_ADMIN -> hasText(scope.getProvinceName())
                    && hasText(scope.getCityName())
                    && hasText(scope.getDistrictName())
                    && hasText(scope.getSchoolName());
            case DISTRICT_ADMIN, DISTRICT_REVIEWER -> hasText(scope.getProvinceName())
                    && hasText(scope.getCityName())
                    && hasText(scope.getDistrictName());
            case CITY_ADMIN, CITY_REVIEWER -> hasText(scope.getProvinceName())
                    && hasText(scope.getCityName());
            case PROVINCE_ADMIN, PROVINCE_REVIEWER -> hasText(scope.getProvinceName());
        };
    }

    private static boolean matchesTeacher(AuthUser user, UserScope userScope, ScopedData data, Long ownerUserId) {
        boolean scopeMatched = equalsName(userScope.getProvinceName(), data.getProvinceName())
                && equalsName(userScope.getCityName(), data.getCityName())
                && equalsName(userScope.getDistrictName(), data.getDistrictName())
                && equalsName(userScope.getSchoolName(), data.getSchoolName());
        if (!scopeMatched) {
            return false;
        }
        if (ownerUserId != null) {
            return ownerUserId.equals(user.getUserId());
        }
        return true;
    }

    public static boolean matchesTeacherOwner(AuthUser user, Long ownerUserId) {
        if (user == null || ownerUserId == null) {
            return false;
        }
        requireScopeConfigured(user);
        return user.getRoleType() == RoleTypeEnum.TEACHER && ownerUserId.equals(user.getUserId());
    }

    public static void requireTeacherOwner(AuthUser user, Long ownerUserId) {
        if (!matchesTeacherOwner(user, ownerUserId)) {
            throw new BusinessException(ErrorCode.SCOPE_ACCESS_DENIED);
        }
    }

    private static boolean matchesSchool(UserScope userScope, ScopedData data) {
        return equalsName(userScope.getProvinceName(), data.getProvinceName())
                && equalsName(userScope.getCityName(), data.getCityName())
                && equalsName(userScope.getDistrictName(), data.getDistrictName())
                && equalsName(userScope.getSchoolName(), data.getSchoolName());
    }

    private static boolean matchesDistrict(UserScope userScope, ScopedData data) {
        return equalsName(userScope.getProvinceName(), data.getProvinceName())
                && equalsName(userScope.getCityName(), data.getCityName())
                && equalsName(userScope.getDistrictName(), data.getDistrictName());
    }

    private static boolean matchesCity(UserScope userScope, ScopedData data) {
        return equalsName(userScope.getProvinceName(), data.getProvinceName())
                && equalsName(userScope.getCityName(), data.getCityName());
    }

    private static boolean matchesProvince(UserScope userScope, ScopedData data) {
        return equalsName(userScope.getProvinceName(), data.getProvinceName());
    }

    public static boolean equalsName(String left, String right) {
        return normalize(left).equals(normalize(right));
    }

    static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean hasText(String value) {
        return !normalize(value).isEmpty();
    }
}
