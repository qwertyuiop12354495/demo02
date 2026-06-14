package com.example.activity.vo.auth;

import com.example.activity.common.enums.UserRole;
import lombok.Data;

@Data
public class LoginVO {

    private String token;

    private UserInfo user;

    @Data
    public static class UserInfo {

        private Long id;

        private String username;

        private String nickname;

        private UserRole role;
    }
}
