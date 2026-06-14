package com.example.activity.service.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.activity.common.auth.AuthUser;
import com.example.activity.common.enums.RoleTypeEnum;
import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;
import com.example.activity.entity.Work;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class WorkReviewScopeQueryBuilder {

    public void applyScope(LambdaQueryWrapper<Work> wrapper, AuthUser user) {
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        switch (user.getRoleType()) {
            case SCHOOL_ADMIN -> {
                requireText(user.getProvinceName(), "省");
                requireText(user.getCityName(), "市");
                requireText(user.getDistrictName(), "区/县");
                requireText(user.getSchoolName(), "学校");
                wrapper.eq(Work::getProvinceName, user.getProvinceName().trim())
                        .eq(Work::getCityName, user.getCityName().trim())
                        .eq(Work::getDistrictName, user.getDistrictName().trim())
                        .eq(Work::getSchoolName, user.getSchoolName().trim());
            }
            case DISTRICT_ADMIN -> {
                requireText(user.getProvinceName(), "省");
                requireText(user.getCityName(), "市");
                requireText(user.getDistrictName(), "区/县");
                wrapper.eq(Work::getProvinceName, user.getProvinceName().trim())
                        .eq(Work::getCityName, user.getCityName().trim())
                        .eq(Work::getDistrictName, user.getDistrictName().trim());
            }
            case CITY_ADMIN -> {
                requireText(user.getProvinceName(), "省");
                requireText(user.getCityName(), "市");
                wrapper.eq(Work::getProvinceName, user.getProvinceName().trim())
                        .eq(Work::getCityName, user.getCityName().trim());
            }
            case PROVINCE_ADMIN -> {
                requireText(user.getProvinceName(), "省");
                wrapper.eq(Work::getProvinceName, user.getProvinceName().trim());
            }
            default -> throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private void requireText(String value, String label) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(ErrorCode.SCOPE_NOT_CONFIGURED, "账号" + label + "辖区未配置");
        }
    }
}
