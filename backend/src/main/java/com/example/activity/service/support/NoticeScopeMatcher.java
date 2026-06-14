package com.example.activity.service.support;

import com.example.activity.common.auth.AuthUser;
import com.example.activity.common.enums.RoleTypeEnum;
import com.example.activity.common.enums.VisibleScopeTypeEnum;
import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;
import com.example.activity.common.scope.ScopeNameMatcher;
import com.example.activity.common.scope.ScopedData;
import com.example.activity.entity.PublicNotice;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class NoticeScopeMatcher {

    public boolean canView(AuthUser user, PublicNotice notice) {
        if (user == null || notice == null) {
            return false;
        }
        if (notice.getPublishTime() == null) {
            return false;
        }
        VisibleScopeTypeEnum scopeType = notice.getVisibleScopeType();
        if (scopeType == VisibleScopeTypeEnum.PUBLIC) {
            return true;
        }
        return matchesNoticeScope(user, notice);
    }

    public void requireView(AuthUser user, PublicNotice notice) {
        if (!canView(user, notice)) {
            throw new BusinessException(ErrorCode.NOTICE_NOT_VISIBLE);
        }
    }

    public boolean matchesWorkScope(AuthUser user, ScopedData work) {
        if (user == null || work == null) {
            return false;
        }
        if (user.getRoleType() == RoleTypeEnum.TEACHER) {
            return ScopeNameMatcher.canAccess(user, work, user.getUserId());
        }
        return ScopeNameMatcher.canAccess(user, work);
    }

    private boolean matchesNoticeScope(AuthUser user, PublicNotice notice) {
        VisibleScopeTypeEnum scopeType = notice.getVisibleScopeType();
        if (scopeType == null) {
            return false;
        }
        return switch (scopeType) {
            case PUBLIC -> true;
            case PROVINCE -> matchesProvince(user, notice);
            case CITY -> matchesProvince(user, notice) && matchesCity(user, notice);
            case DISTRICT -> matchesProvince(user, notice) && matchesCity(user, notice)
                    && matchesDistrict(user, notice);
            case SCHOOL -> matchesProvince(user, notice) && matchesCity(user, notice)
                    && matchesDistrict(user, notice) && matchesSchool(user, notice);
        };
    }

    private boolean matchesProvince(AuthUser user, PublicNotice notice) {
        return equalsName(user.getProvinceName(), notice.getProvinceName());
    }

    private boolean matchesCity(AuthUser user, PublicNotice notice) {
        return equalsName(user.getCityName(), notice.getCityName());
    }

    private boolean matchesDistrict(AuthUser user, PublicNotice notice) {
        return equalsName(user.getDistrictName(), notice.getDistrictName());
    }

    private boolean matchesSchool(AuthUser user, PublicNotice notice) {
        return equalsName(user.getSchoolName(), notice.getSchoolName());
    }

    private boolean equalsName(String left, String right) {
        return normalize(left).equals(normalize(right));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    public void validateScopeFields(VisibleScopeTypeEnum scopeType,
                                      String provinceName, String cityName,
                                      String districtName, String schoolName) {
        switch (scopeType) {
            case PUBLIC -> {
            }
            case PROVINCE -> requireText(provinceName, "省");
            case CITY -> {
                requireText(provinceName, "省");
                requireText(cityName, "市");
            }
            case DISTRICT -> {
                requireText(provinceName, "省");
                requireText(cityName, "市");
                requireText(districtName, "区/县");
            }
            case SCHOOL -> {
                requireText(provinceName, "省");
                requireText(cityName, "市");
                requireText(districtName, "区/县");
                requireText(schoolName, "学校");
            }
        }
    }

    private void requireText(String value, String label) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "可见范围" + label + "不能为空");
        }
    }
}
