package com.example.activity.service;

import com.example.activity.dto.request.auth.LoginRequest;
import com.example.activity.vo.auth.LoginVO;

public interface AuthService {

    LoginVO login(LoginRequest request);
}
