package com.example.activity.common.auth;

import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;

public final class UserContext {

    private UserContext() {
    }

    public static AuthUser getAuthUser() {
        return AuthContext.get();
    }

    public static Long getUserId() {
        return AuthContext.getUserId();
    }

    public static Long requireUserId() {
        Long userId = getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return userId;
    }

    /** @deprecated 请使用 {@link AuthContext#get()} */
    @Deprecated
    public static LoginUser get() {
        AuthUser authUser = AuthContext.get();
        return authUser != null ? LoginUser.fromAuthUser(authUser) : null;
    }

    /** @deprecated 请使用 {@link AuthContext#set(AuthUser)} */
    @Deprecated
    public static void set(LoginUser user) {
        if (user == null) {
            AuthContext.clear();
            return;
        }
        AuthContext.set(AuthUser.of(
                user.getId(),
                user.getUsername(),
                com.example.activity.common.enums.RoleTypeEnum.fromLegacyUserRole(user.getRole()),
                null, null, null, null
        ));
    }

    public static void clear() {
        AuthContext.clear();
    }
}
