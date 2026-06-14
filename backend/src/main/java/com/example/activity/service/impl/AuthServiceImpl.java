package com.example.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.activity.common.auth.AuthUser;
import com.example.activity.common.auth.JwtTokenProvider;
import com.example.activity.common.enums.RoleTypeEnum;
import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;
import com.example.activity.dto.request.auth.LoginRequest;
import com.example.activity.entity.SysUser;
import com.example.activity.mapper.SysUserMapper;
import com.example.activity.service.AuthService;
import com.example.activity.vo.auth.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public LoginVO login(LoginRequest request) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "用户名或密码错误");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "用户名或密码错误");
        }

        RoleTypeEnum roleType = RoleTypeEnum.fromValue(user.getRole());
        AuthUser authUser = AuthUser.of(
                user.getId(),
                user.getUsername(),
                roleType,
                user.getProvinceName(),
                user.getCityName(),
                user.getDistrictName(),
                user.getSchoolName()
        );
        LoginVO vo = new LoginVO();
        vo.setToken(jwtTokenProvider.generateToken(authUser));

        LoginVO.UserInfo userInfo = new LoginVO.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setRole(roleType.toLegacy());
        vo.setUser(userInfo);
        return vo;
    }
}
