//package com.banksphere.controller;
//
//import com.banksphere.dto.ApiResponse;
//import io.swagger.v3.oas.annotations.Operation;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.stream.Collectors;
//
//@RestController
//public class TestSecureController {
//
//    @Operation(summary = "Test secured endpoint")
//    @GetMapping("/api/secure")
//    public ApiResponse<String> secure() {
//        return ApiResponse.ok("Access granted", "You are authenticated");
//    }
//
//    @Operation(summary = "Test admin-only endpoint")
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
//    @GetMapping("/api/admin-only")
//    public ApiResponse<String> adminOnly() {
//        return ApiResponse.ok("Admin access granted", "Only ADMIN can see this");
//    }
//
//    @Operation(summary = "Get current username and roles")
//    @GetMapping("/api/whoami")
//    public ApiResponse<String> whoami(Authentication auth) {
//        String roles = auth.getAuthorities().stream()
//                .map(GrantedAuthority::getAuthority)
//                .collect(Collectors.joining(", "));
//        return ApiResponse.ok(auth.getName(), roles);
//    }
//}