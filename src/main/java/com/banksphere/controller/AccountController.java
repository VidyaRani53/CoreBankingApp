package com.banksphere.controller;

import com.banksphere.dto.ApiResponse;
import com.banksphere.dto.account.AccountResponse;
import com.banksphere.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "Get my accounts")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @GetMapping("/me")
    public ApiResponse<List<AccountResponse>> myAccounts() {
        return ApiResponse.ok("My accounts fetched", accountService.myAccounts());
    }

    @Operation(summary = "Get account by ID")
    @GetMapping("/{accountId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_BRANCH_MANAGER','ROLE_CSR','ROLE_CUSTOMER')")
    public ApiResponse<AccountResponse> getAccountById(@PathVariable UUID accountId) {
        return ApiResponse.ok(
                "Account fetched successfully",
                accountService.getAccountById(accountId)
        );
    }

    @Operation(summary = "Get accounts by customer ID")
    @GetMapping("/by-customer/{customerId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_BRANCH_MANAGER','ROLE_CSR','ROLE_CUSTOMER')")
    public ApiResponse<List<AccountResponse>> getAccountsByCustomerId(@PathVariable UUID customerId) {
        return ApiResponse.ok(
                "Customer accounts fetched successfully",
                accountService.getAccountsByCustomerId(customerId)
        );
    }

    @Operation(summary = "Get my branch accounts")
    @GetMapping("/my-branch")
    @PreAuthorize("hasAuthority('ROLE_BRANCH_MANAGER')")
    public ApiResponse<List<AccountResponse>> getMyBranchAccounts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String accountType,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        return ApiResponse.ok(
                "Branch accounts fetched successfully",
                accountService.getMyBranchAccounts(status, accountType, sortBy, sortDir)
        );
    }

    @Operation(summary = "Close account (soft close)")
    @PatchMapping("/{accountNo}/close")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_BRANCH_MANAGER')")
    public ApiResponse<AccountResponse> closeAccount(@PathVariable String accountNo) {
        return ApiResponse.ok(
                "Account closed successfully",
                accountService.closeAccount(accountNo)
        );
    }

    @Operation(summary = "Freeze account")
    @PatchMapping("/{accountNo}/freeze")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_BRANCH_MANAGER')")
    public ApiResponse<AccountResponse> freezeAccount(@PathVariable String accountNo) {
        return ApiResponse.ok(
                "Account frozen",
                accountService.freezeAccount(accountNo)
        );
    }

    @Operation(summary = "Unfreeze account")
    @PatchMapping("/{accountNo}/unfreeze")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_BRANCH_MANAGER')")
    public ApiResponse<AccountResponse> unfreezeAccount(@PathVariable String accountNo) {
        return ApiResponse.ok(
                "Account reactivated",
                accountService.unfreezeAccount(accountNo)
        );
    }
}