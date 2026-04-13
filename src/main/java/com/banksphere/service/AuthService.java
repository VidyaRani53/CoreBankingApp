package com.banksphere.service;

import com.banksphere.dto.LoginRequest;
import com.banksphere.dto.LoginResponse;
import com.banksphere.dto.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
}