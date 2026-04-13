package com.banksphere.controller;

import com.banksphere.dto.ApiResponse;
import com.banksphere.dto.account.AccountResponse;
import com.banksphere.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @GetMapping("/me")
    public ApiResponse<List<AccountResponse>> myAccounts() {
        return ApiResponse.ok("My accounts fetched", accountService.myAccounts());
    }
}