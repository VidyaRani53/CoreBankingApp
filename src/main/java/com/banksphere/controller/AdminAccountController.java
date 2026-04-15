package com.banksphere.controller;

import com.banksphere.dto.ApiResponse;
import com.banksphere.dto.account.AccountResponse;
import com.banksphere.service.AdminAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/accounts")
@RequiredArgsConstructor
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    /**
     * Admin / Branch Manager can view all accounts in a branch.
     *
     * ADMIN  -> must pass branchCode
     * MANAGER -> branchCode is resolved from logged-in user
     *
     * Optional filters:
     *  - status (ACTIVE / INACTIVE)
     *  - accountType (SAVINGS / CURRENT)
     *  - sorting (accountType / openedAt)
     */
    @GetMapping("/by-branch")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_BRANCH_MANAGER')")
    public ApiResponse<List<AccountResponse>> getAccountsByBranch(
            @RequestParam(required = false) String branchCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String accountType,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        List<AccountResponse> response =
                adminAccountService.getAccountsByBranch(
                        branchCode,
                        status,
                        accountType,
                        sortBy,
                        sortDir
                );

        return ApiResponse.ok("Accounts fetched successfully", response);
    }
}