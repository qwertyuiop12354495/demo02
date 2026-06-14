package com.example.activity.common.auth;

import com.example.activity.common.enums.RoleTypeEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AuthContextTest {

    @AfterEach
    void tearDown() {
        AuthContext.clear();
    }

    @Test
    void setAndGet_shouldStoreAuthUser() {
        AuthUser user = AuthUser.of(1L, "teacher1", RoleTypeEnum.TEACHER,
                "广东省", "深圳市", "南山区", "示例中学");
        AuthContext.set(user);

        assertEquals(user, AuthContext.get());
        assertEquals(1L, AuthContext.getUserId());
        assertEquals(1L, AuthContext.requireUserId());
    }

    @Test
    void clear_shouldRemoveThreadLocal() {
        AuthContext.set(AuthUser.of(1L, "u", RoleTypeEnum.TEACHER,
                "广东省", "深圳市", "南山区", "示例中学"));
        AuthContext.clear();
        assertNull(AuthContext.get());
    }
}
