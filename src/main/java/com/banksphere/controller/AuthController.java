package com.banksphere.controller;

import com.banksphere.dto.*;
import com.banksphere.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.ok("Registered successfully", null);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("Login successful", authService.login(request));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ApiResponse<MeResponse> me(Authentication authentication) {
        var authorities = authentication.getAuthorities()
                .stream()
                .map(a -> a.getAuthority())
                .toList();

        return ApiResponse.ok("Current user", MeResponse.builder()
                .username(authentication.getName())
                .authorities(authorities)
                .build());
    }
}