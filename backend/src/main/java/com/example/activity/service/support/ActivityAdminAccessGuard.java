package com.example.activity.service.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.activity.common.auth.AuthContext;
import com.example.activity.common.auth.AuthUser;
import com.example.activity.common.auth.RoleGuard;
import com.example.activity.common.enums.RoleTypeEnum;
import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;
import com.example.activity.common.scope.ScopeNameMatcher;
import com.example.activity.entity.Activity;
import com.example.activity.entity.SysUser;
import com.example.activity.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivityAdminAccessGuard {

    private final SysUserMapper sysUserMapper;

    public AuthUser requireProvinceAdmin() {
        RoleGuard.requireRole(RoleTypeEnum.PROVINCE_ADMIN);
        AuthUser user = AuthContext.require();
        ScopeNameMatcher.requireScopeConfigured(user);
        return user;
    }

    public void applyProvinceScope(LambdaQueryWrapper<Activity> wrapper, AuthUser admin) {
        requireProvinceAdmin();
        wrapper.apply("created_by IN (SELECT id FROM sys_user WHERE province_name = {0})",
                admin.getProvinceName().trim());
    }

    public void requireActivityInProvince(Activity activity, AuthUser admin) {
        requireProvinceAdmin();
        if (activity.getCreatedBy() == null) {
            throw new BusinessException(ErrorCode.SCOPE_ACCESS_DENIED);
        }
        SysUser creator = sysUserMapper.selectById(activity.getCreatedBy());
        if (creator == null
                || !ScopeNameMatcher.equalsName(creator.getProvinceName(), admin.getProvinceName())) {
            throw new BusinessException(ErrorCode.SCOPE_ACCESS_DENIED);
        }
    }
}
