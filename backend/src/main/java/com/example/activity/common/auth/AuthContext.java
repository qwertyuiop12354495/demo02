package com.example.activity.common.auth;

import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;

public final class AuthContext {

    private static final ThreadLocal<AuthUser> HOLDER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(AuthUser user) {
        HOLDER.set(user);
    }

    public static AuthUser get() {
        return HOLDER.get();
    }

    public static AuthUser require() {
        AuthUser user = get();
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return user;
    }

    public static Long getUserId() {
        AuthUser user = get();
        return user != null ? user.getUserId() : null;
    }

    public static Long requireUserId() {
        return require().getUserId();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
