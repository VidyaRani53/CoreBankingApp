//package com.banksphere.controller;
//
//import com.banksphere.dto.ApiResponse;
//import com.banksphere.dto.txn.*;
//import com.banksphere.service.TransactionService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/transactions")
//@RequiredArgsConstructor
//public class TransactionController {
//
//    private final TransactionService transactionService;
//
//    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
//    @PostMapping("/deposit")
//    public ApiResponse<TxnResponse> deposit(@Valid @RequestBody DepositRequest request) {
//        return ApiResponse.ok("Deposit posted", transactionService.deposit(request));
//    }
//
//    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
//    @PostMapping("/withdraw")
//    public ApiResponse<TxnResponse> withdraw(@Valid @RequestBody WithdrawRequest request) {
//        return ApiResponse.ok("Withdrawal posted", transactionService.withdraw(request));
//    }
//
//    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
//    @PostMapping("/transfer")
//    public ApiResponse<TxnResponse> transfer(@Valid @RequestBody TransferRequest request) {
//        return ApiResponse.ok("Transfer posted", transactionService.transfer(request));
//    }
//
//    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
//    @GetMapping("/accounts/{accountId}")
//    public ApiResponse<List<TxnResponse>> accountTxns(@PathVariable UUID accountId,
//                                                      @RequestParam LocalDate from,
//                                                      @RequestParam LocalDate to,
//                                                      @RequestParam(required = false) String type) {
//        return ApiResponse.ok("Transactions fetched",
//                transactionService.accountTransactions(accountId, from, to, type));
//    }
//}