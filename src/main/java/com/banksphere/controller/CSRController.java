package com.banksphere.controller;

import com.banksphere.dto.ApiResponse;
import com.banksphere.dto.account.AccountResponse;
import com.banksphere.dto.customer.CustomerResponse;
import com.banksphere.dto.txn.TxnResponse;
import com.banksphere.entity.CustomerUpdateRequest;
import com.banksphere.service.TransactionService;
import com.banksphere.service.impl.AccountServiceImpl;
import com.banksphere.service.impl.CSRServiceImpl;
import com.banksphere.service.impl.CustomerServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/customer-update-requests")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_CSR')")
public class CSRController {

    private final TransactionService transactionService;
    private final CSRServiceImpl csrservice;
    private final CustomerServiceImpl customerService;
    private final AccountServiceImpl accountService;
    @GetMapping("/pending")
    public ApiResponse<List<CustomerUpdateRequest>> pendingRequests() {
        return ApiResponse.ok(
                "Pending customer update requests",
                csrservice.getPendingRequests()
        );
    }

    @PostMapping("/{id}/approveprofileupdate")
    public ApiResponse<Void> approve(@PathVariable Long id) {
        csrservice.approveUpdate(id);
        return ApiResponse.ok("Customer profile update approved",null);
    }

    @PostMapping("/{id}/rejectprofileupdate")
    public ApiResponse<Void> reject(
            @PathVariable Long id,
            @RequestParam String reason) {

        csrservice.rejectUpdate(id, reason);
        return ApiResponse.ok("Customer profile update rejected",null);
    }
    @GetMapping("/pending-approval")
    @PreAuthorize("hasAuthority('ROLE_CSR')")
    public ApiResponse<List<TxnResponse>> pendingApprovals() {
        return ApiResponse.ok("Pending approvals fetched",
                transactionService.getPendingHighValueTxns());
    }

    @PostMapping("/{txnId}/approvetxn")
    @PreAuthorize("hasAuthority('ROLE_CSR')")
    public ApiResponse<TxnResponse> approve(@PathVariable UUID txnId) {
        return ApiResponse.ok("Transaction approved",
                transactionService.approveHighValueTxn(txnId));
    }

    @PostMapping("/{txnId}/rejecttxn")
    @PreAuthorize("hasAuthority('ROLE_CSR')")
    public ApiResponse<TxnResponse> reject(@PathVariable UUID txnId,
                                           @RequestParam String reason) {
        return ApiResponse.ok("Transaction rejected",
                transactionService.rejectHighValueTxn(txnId, reason));
    }

//    @GetMapping("/my-branch")
//    @PreAuthorize("hasAnyAuthority('ROLE_CSR','ROLE_BRANCH_MANAGER')")
//    public ApiResponse<List<AccountResponse>> getMyBranchAccounts() {
//
//        return ApiResponse.ok(
//                "Branch accounts fetched successfully",
//                accountService.getAccountsForMyBranch()
//        );
//    }



}