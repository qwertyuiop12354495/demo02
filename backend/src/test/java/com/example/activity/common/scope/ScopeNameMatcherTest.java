package com.example.activity.common.scope;

import com.example.activity.common.auth.AuthUser;
import com.example.activity.common.enums.RoleTypeEnum;
import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScopeNameMatcherTest {

    private static final AuthUser TEACHER = AuthUser.of(
            10L, "teacher", RoleTypeEnum.TEACHER,
            "广东省", "深圳市", "南山区", "示例中学"
    );

    private static final AuthUser PROVINCE_ADMIN = AuthUser.of(
            1L, "province", RoleTypeEnum.PROVINCE_ADMIN,
            "广东省", null, null, null
    );

    private static final AuthUser DISTRICT_ADMIN = AuthUser.of(
            2L, "district", RoleTypeEnum.DISTRICT_ADMIN,
            "广东省", "深圳市", "南山区", null
    );

    @Test
    void canAccess_teacherSameSchool_shouldPass() {
        assertTrue(ScopeNameMatcher.canAccess(TEACHER, sampleData("广东省", "深圳市", "南山区", "示例中学")));
    }

    @Test
    void canAccess_teacherOtherSchool_shouldFail() {
        assertFalse(ScopeNameMatcher.canAccess(TEACHER, sampleData("广东省", "深圳市", "南山区", "其他中学")));
    }

    @Test
    void canAccess_teacherOwnWork_shouldPass() {
        assertTrue(ScopeNameMatcher.canAccess(
                TEACHER,
                sampleData("广东省", "深圳市", "南山区", "示例中学"),
                10L
        ));
    }

    @Test
    void canAccess_teacherOtherWork_shouldFail() {
        assertFalse(ScopeNameMatcher.canAccess(
                TEACHER,
                sampleData("广东省", "深圳市", "南山区", "示例中学"),
                99L
        ));
    }

    @Test
    void canAccess_provinceAdminSameProvince_shouldPass() {
        assertTrue(ScopeNameMatcher.canAccess(
                PROVINCE_ADMIN,
                sampleData("广东省", "广州市", "天河区", "任意中学")
        ));
    }

    @Test
    void canAccess_provinceAdminOtherProvince_shouldFail() {
        assertFalse(ScopeNameMatcher.canAccess(
                PROVINCE_ADMIN,
                sampleData("浙江省", "杭州市", "西湖区", "任意中学")
        ));
    }

    @Test
    void canAccess_districtAdminSameDistrict_shouldPass() {
        assertTrue(ScopeNameMatcher.canAccess(
                DISTRICT_ADMIN,
                sampleData("广东省", "深圳市", "南山区", "任意中学")
        ));
    }

    @Test
    void canAccess_unconfiguredScope_shouldFail() {
        AuthUser teacherWithoutScope = AuthUser.of(
                11L, "t2", RoleTypeEnum.TEACHER, null, null, null, null
        );
        assertFalse(ScopeNameMatcher.canAccess(
                teacherWithoutScope,
                sampleData("广东省", "深圳市", "南山区", "示例中学")
        ));
    }

    @Test
    void requireScopeConfigured_shouldThrowWhenMissing() {
        AuthUser user = AuthUser.of(1L, "u", RoleTypeEnum.PROVINCE_ADMIN, null, null, null, null);
        BusinessException ex = assertThrows(BusinessException.class, () -> ScopeNameMatcher.requireScopeConfigured(user));
        assertEquals(ErrorCode.SCOPE_NOT_CONFIGURED.getCode(), ex.getCode());
    }

    @Test
    void equalsName_shouldTrimAndMatch() {
        assertTrue(ScopeNameMatcher.equalsName(" 示例中学 ", "示例中学"));
    }

    private static ScopedData sampleData(String province, String city, String district, String school) {
        return new ScopedData() {
            @Override
            public String getProvinceName() {
                return province;
            }

            @Override
            public String getCityName() {
                return city;
            }

            @Override
            public String getDistrictName() {
                return district;
            }

            @Override
            public String getSchoolName() {
                return school;
            }
        };
    }
}
