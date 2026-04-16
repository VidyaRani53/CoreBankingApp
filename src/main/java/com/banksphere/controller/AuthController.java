package com.banksphere.controller;

import com.banksphere.dto.*;
import com.banksphere.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register new customer")
    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.ok("Registered successfully", null);
    }

    @Operation(summary = "Login (get JWT)")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("Login successful", authService.login(request));
    }

    @Operation(summary = "Get current user")
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