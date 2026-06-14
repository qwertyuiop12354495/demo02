package com.example.activity.common.auth;

import com.example.activity.common.enums.RoleTypeEnum;
import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoleGuardTest {

    @AfterEach
    void tearDown() {
        AuthContext.clear();
    }

    @Test
    void requireRole_shouldPassWhenRoleMatches() {
        AuthContext.set(AuthUser.of(1L, "admin", RoleTypeEnum.PROVINCE_ADMIN,
                "广东省", "深圳市", null, null));
        assertDoesNotThrow(() -> RoleGuard.requireRole(RoleTypeEnum.PROVINCE_ADMIN));
    }

    @Test
    void requireRole_shouldRejectWhenRoleMismatch() {
        AuthContext.set(AuthUser.of(1L, "teacher", RoleTypeEnum.TEACHER,
                "广东省", "深圳市", "南山区", "示例中学"));
        BusinessException ex = assertThrows(BusinessException.class,
                () -> RoleGuard.requireRole(RoleTypeEnum.PROVINCE_ADMIN));
        assertEquals(ErrorCode.FORBIDDEN.getCode(), ex.getCode());
    }

    @Test
    void requireAnyRole_shouldPassWhenOneRoleMatches() {
        AuthContext.set(AuthUser.of(1L, "district", RoleTypeEnum.DISTRICT_ADMIN,
                "广东省", "深圳市", "南山区", null));
        assertDoesNotThrow(() -> RoleGuard.requireAnyRole(
                RoleTypeEnum.PROVINCE_ADMIN,
                RoleTypeEnum.DISTRICT_ADMIN
        ));
    }

    @Test
    void requireAnyRole_shouldRejectWhenNoContext() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> RoleGuard.requireAnyRole(RoleTypeEnum.TEACHER));
        assertEquals(ErrorCode.UNAUTHORIZED.getCode(), ex.getCode());
    }

    @Test
    void requireLegacyAdmin_shouldAcceptProvinceAdmin() {
        AuthContext.set(AuthUser.of(1L, "admin", RoleTypeEnum.PROVINCE_ADMIN,
                "广东省", null, null, null));
        assertDoesNotThrow(RoleGuard::requireLegacyAdmin);
    }
}
