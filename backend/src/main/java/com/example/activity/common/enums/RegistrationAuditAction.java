package com.example.activity.common.enums;

import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;

public enum RegistrationAuditAction {

    APPROVE,
    REJECT;

    public static RegistrationAuditAction from(String action) {
        if (action == null || action.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "审核动作不能为空");
        }
        try {
            return valueOf(action.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "action 参数非法");
        }
    }
}
