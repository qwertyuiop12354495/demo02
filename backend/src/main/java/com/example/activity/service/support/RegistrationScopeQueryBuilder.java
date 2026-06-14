package com.example.activity.service.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.activity.common.auth.AuthUser;
import com.example.activity.common.enums.RoleTypeEnum;
import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;
import com.example.activity.common.scope.ScopeNameMatcher;
import com.example.activity.entity.ActivityRegistration;
import com.example.activity.entity.SysUser;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class RegistrationScopeQueryBuilder {

    public void applyRegistrantScope(LambdaQueryWrapper<ActivityRegistration> wrapper, AuthUser admin) {
        if (admin == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        ScopeNameMatcher.requireScopeConfigured(admin);
        switch (admin.getRoleType()) {
            case SCHOOL_ADMIN -> {
                requireText(admin.getProvinceName(), "省");
                requireText(admin.getCityName(), "市");
                requireText(admin.getDistrictName(), "区/县");
                requireText(admin.getSchoolName(), "学校");
                wrapper.apply(
                        "user_id IN (SELECT id FROM sys_user WHERE province_name = {0} "
                                + "AND city_name = {1} AND district_name = {2} AND school_name = {3})",
                        admin.getProvinceName().trim(),
                        admin.getCityName().trim(),
                        admin.getDistrictName().trim(),
                        admin.getSchoolName().trim());
            }
            case DISTRICT_ADMIN -> {
                requireText(admin.getProvinceName(), "省");
                requireText(admin.getCityName(), "市");
                requireText(admin.getDistrictName(), "区/县");
                wrapper.apply(
                        "user_id IN (SELECT id FROM sys_user WHERE province_name = {0} "
                                + "AND city_name = {1} AND district_name = {2})",
                        admin.getProvinceName().trim(),
                        admin.getCityName().trim(),
                        admin.getDistrictName().trim());
            }
            case CITY_ADMIN -> {
                requireText(admin.getProvinceName(), "省");
                requireText(admin.getCityName(), "市");
                wrapper.apply(
                        "user_id IN (SELECT id FROM sys_user WHERE province_name = {0} AND city_name = {1})",
                        admin.getProvinceName().trim(),
                        admin.getCityName().trim());
            }
            case PROVINCE_ADMIN -> {
                requireText(admin.getProvinceName(), "省");
                wrapper.apply("user_id IN (SELECT id FROM sys_user WHERE province_name = {0})",
                        admin.getProvinceName().trim());
            }
            default -> throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    public void requireRegistrantAccess(AuthUser admin, SysUser registrant) {
        if (admin == null || registrant == null) {
            throw new BusinessException(ErrorCode.SCOPE_ACCESS_DENIED);
        }
        ScopeNameMatcher.requireAccess(admin, registrant);
    }

    private void requireText(String value, String label) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(ErrorCode.SCOPE_NOT_CONFIGURED, "账号" + label + "辖区未配置");
        }
    }
}
