package com.example.activity.common.auth;

import com.example.activity.common.enums.RoleTypeEnum;
import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public final class RoleGuard {

    private static final Set<RoleTypeEnum> LEGACY_ADMIN_ROLES = Set.of(
            RoleTypeEnum.SCHOOL_ADMIN,
            RoleTypeEnum.DISTRICT_ADMIN,
            RoleTypeEnum.CITY_ADMIN,
            RoleTypeEnum.PROVINCE_ADMIN
    );

    private RoleGuard() {
    }

    public static void requireRole(RoleTypeEnum... roles) {
        AuthUser user = AuthContext.require();
        if (roles == null || roles.length == 0) {
            throw new IllegalArgumentException("At least one role is required");
        }
        Set<RoleTypeEnum> allowed = Set.copyOf(Arrays.asList(roles));
        if (!allowed.contains(user.getRoleType())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    public static void requireAnyRole(RoleTypeEnum... roles) {
        AuthUser user = AuthContext.require();
        if (roles == null || roles.length == 0) {
            throw new IllegalArgumentException("At least one role is required");
        }
        Set<RoleTypeEnum> allowed = Arrays.stream(roles).collect(Collectors.toUnmodifiableSet());
        if (!allowed.contains(user.getRoleType())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    public static void requireLegacyAdmin() {
        requireAnyRole(LEGACY_ADMIN_ROLES.toArray(RoleTypeEnum[]::new));
    }
}
