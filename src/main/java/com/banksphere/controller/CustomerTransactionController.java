package com.banksphere.controller;

import com.banksphere.dto.ApiResponse;
import com.banksphere.dto.txn.TransferRequest;
import com.banksphere.dto.txn.TxnResponse;
import com.banksphere.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/customer/transactions")
@RequiredArgsConstructor
public class CustomerTransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "Transfer funds")
    @PostMapping("/transfer")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    public ApiResponse<TxnResponse> transfer(@Valid @RequestBody TransferRequest request) {
        return ApiResponse.ok("Transfer initiated successfully",
                transactionService.transfer(request));
    }

    @Operation(summary = "Get account statement")
    @GetMapping("/my-statement")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    public ApiResponse<List<TxnResponse>> getStatement(
            @RequestParam String accountNo,
            @RequestParam(defaultValue = "3MONTH") String interval,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ApiResponse.ok("Transaction statement fetched successfully",
                transactionService.getStatement(accountNo, interval, from, to, null));
    }
}