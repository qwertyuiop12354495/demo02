package com.example.activity.common.auth;

import com.example.activity.common.enums.RoleTypeEnum;
import com.example.activity.common.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser {

    private Long id;

    private String username;

    private UserRole role;

    public static LoginUser fromAuthUser(AuthUser authUser) {
        return new LoginUser(
                authUser.getUserId(),
                authUser.getUsername(),
                authUser.getRoleType().toLegacy()
        );
    }
}
