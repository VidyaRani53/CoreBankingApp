package com.banksphere.controller;

import com.banksphere.dto.ApiResponse;
import com.banksphere.dto.kyc.KycResponse;
import com.banksphere.dto.kyc.KycSubmitRequest;
import com.banksphere.service.KycService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kyc")
@RequiredArgsConstructor
public class KycController {

    private final KycService kycService;

    @Operation(summary = "Submit KYC")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @PostMapping
    public ApiResponse<KycResponse> submit(@Valid @RequestBody KycSubmitRequest request) {
        return ApiResponse.ok("KYC submitted", kycService.submit(request));
    }

    @Operation(summary = "Get my KYC history")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @GetMapping("/my")
    public ApiResponse<List<KycResponse>> myHistory() {
        return ApiResponse.ok("KYC history fetched", kycService.myHistory());
    }
}