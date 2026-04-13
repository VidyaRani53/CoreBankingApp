package com.banksphere.controller;

import com.banksphere.dto.ApiResponse;
import com.banksphere.dto.account.*;
import com.banksphere.service.AccountApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/account-applications")
@RequiredArgsConstructor
public class AccountApplicationController {

    private final AccountApplicationService accountApplicationService;

    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @PostMapping
    public ApiResponse<AccountApplicationResponse> submit(@Valid @RequestBody AccountApplicationSubmitRequest request) {
        return ApiResponse.ok("Account application submitted", accountApplicationService.submit(request));
    }

    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @GetMapping("/me")
    public ApiResponse<List<AccountApplicationResponse>> myApplications() {
        return ApiResponse.ok("My account applications fetched", accountApplicationService.myApplications());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CSR','ROLE_BRANCH_MANAGER')")
    @GetMapping("/pending")
    public ApiResponse<List<AccountApplicationResponse>> pendingForMyBranch() {
        return ApiResponse.ok("Pending applications fetched", accountApplicationService.pendingForMyBranch());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CSR','ROLE_BRANCH_MANAGER')")
    @PostMapping("/{id}/approve")
    public ApiResponse<AccountResponse> approve(@PathVariable UUID id,
                                                @Valid @RequestBody ApplicationReviewRequest request) {
        return ApiResponse.ok("Application approved and account created", accountApplicationService.approve(id, request));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CSR','ROLE_BRANCH_MANAGER')")
    @PostMapping("/{id}/reject")
    public ApiResponse<AccountApplicationResponse> reject(@PathVariable UUID id,
                                                          @Valid @RequestBody ApplicationReviewRequest request) {
        return ApiResponse.ok("Application rejected", accountApplicationService.reject(id, request));
    }
}