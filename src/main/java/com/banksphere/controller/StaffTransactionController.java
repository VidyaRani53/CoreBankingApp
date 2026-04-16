package com.banksphere.controller;

import com.banksphere.dto.ApiResponse;
import com.banksphere.dto.txn.DepositRequest;
import com.banksphere.dto.txn.TxnResponse;
import com.banksphere.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/staff/transactions")
@RequiredArgsConstructor
public class StaffTransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "Deposit to account")
    @PostMapping("/deposit")
    @PreAuthorize("hasAnyAuthority('ROLE_CSR', 'ROLE_BRANCH_MANAGER')")
    public ApiResponse<TxnResponse> deposit(@Valid @RequestBody DepositRequest request) {
        return ApiResponse.ok("Deposit successful", transactionService.deposit(request));
    }

    @Operation(summary = "Get account transaction history")
    @GetMapping("/history/{accountNo}")
    @PreAuthorize("hasAnyAuthority('ROLE_CSR', 'ROLE_BRANCH_MANAGER', 'ROLE_ADMIN')")
    public ApiResponse<List<TxnResponse>> getAnyStatement(
            @PathVariable String accountNo,
            @RequestParam(required = false) String interval,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {

        return ApiResponse.ok("Account history fetched",
                transactionService.getStatement(accountNo, interval, from, to, null));
    }
}