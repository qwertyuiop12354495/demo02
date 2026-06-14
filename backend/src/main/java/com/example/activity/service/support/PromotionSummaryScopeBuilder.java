package com.example.activity.service.support;

import com.example.activity.common.auth.AuthUser;
import com.example.activity.common.enums.RoleTypeEnum;
import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;
import com.example.activity.common.scope.ScopeNameMatcher;
import lombok.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PromotionSummaryScopeBuilder {

    public ScopeParams buildWorkScopeFilter(AuthUser user) {
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        ScopeNameMatcher.requireScopeConfigured(user);

        return switch (user.getRoleType()) {
            case TEACHER, SCHOOL_ADMIN -> new ScopeParams(
                    trim(user.getProvinceName()),
                    trim(user.getCityName()),
                    trim(user.getDistrictName()),
                    trim(user.getSchoolName()));
            case DISTRICT_ADMIN, DISTRICT_REVIEWER -> new ScopeParams(
                    trim(user.getProvinceName()),
                    trim(user.getCityName()),
                    trim(user.getDistrictName()),
                    null);
            case CITY_ADMIN, CITY_REVIEWER -> new ScopeParams(
                    trim(user.getProvinceName()),
                    trim(user.getCityName()),
                    null,
                    null);
            case PROVINCE_ADMIN, PROVINCE_REVIEWER -> new ScopeParams(
                    trim(user.getProvinceName()),
                    null,
                    null,
                    null);
        };
    }

    private String trim(String value) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(ErrorCode.SCOPE_NOT_CONFIGURED);
        }
        return value.trim();
    }

    @Value
    public static class ScopeParams {
        String provinceName;
        String cityName;
        String districtName;
        String schoolName;
    }
}
