package com.banksphere.controller;

import com.banksphere.dto.ApiResponse;
import com.banksphere.dto.kyc.KycResponse;
import com.banksphere.dto.kyc.KycReviewRequest;
import com.banksphere.service.KycService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employee/kyc")
@RequiredArgsConstructor
public class EmployeeKycController {

    private final KycService kycService;

    @PreAuthorize("hasAuthority('ROLE_CSR') or hasAuthority('ROLE_BRANCH_MANAGER')")
    @GetMapping("/inbox")
    public ApiResponse<List<KycResponse>> inbox() {
        return ApiResponse.ok("KYC inbox fetched", kycService.inbox());
    }

    @PreAuthorize("hasAuthority('ROLE_CSR') or hasAuthority('ROLE_BRANCH_MANAGER')")
    @PatchMapping("/{id}/review")
    public ApiResponse<KycResponse> review(@PathVariable("id") Long id,
                                           @Valid @RequestBody KycReviewRequest request) {
        return ApiResponse.ok("KYC reviewed", kycService.review(id, request));
    }
}