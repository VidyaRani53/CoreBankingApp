package com.banksphere.controller;

import com.banksphere.dto.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.stream.Collectors;
@RestController
public class TestSecureController {

    @GetMapping("/api/secure")
    public ApiResponse<String> secure() {
        return ApiResponse.ok("Access granted", "You are authenticated");
    }

   // @PreAuthorize("hasRole('ADMIN')")
   @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/api/admin-only")
    public ApiResponse<String> adminOnly() {
        return ApiResponse.ok("Admin access granted", "Only ADMIN can see this");
    }
    @GetMapping("/api/whoami")
    public ApiResponse<String> whoami(Authentication auth) {
        String roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(", "));
        return ApiResponse.ok(auth.getName(), roles);
    }
}