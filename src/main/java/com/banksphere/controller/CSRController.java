package com.banksphere.controller;

import com.banksphere.dto.ApiResponse;
import com.banksphere.dto.txn.TxnResponse;
import com.banksphere.entity.CustomerUpdateRequest;
import com.banksphere.service.TransactionService;
import com.banksphere.service.impl.CSRServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "Get pending customer profile update requests")
    @GetMapping("/pending")
    public ApiResponse<List<CustomerUpdateRequest>> pendingRequests() {
        return ApiResponse.ok(
                "Pending customer update requests",
                csrservice.getPendingRequests()
        );
    }

    @Operation(summary = "Approve customer profile update request")
    @PostMapping("/{id}/approveprofileupdate")
    public ApiResponse<Void> approveProfileUpdate(@PathVariable Long id) {
        csrservice.approveUpdate(id);
        return ApiResponse.ok("Customer profile update approved", null);
    }

    @Operation(summary = "Reject customer profile update request")
    @PostMapping("/{id}/rejectprofileupdate")
    public ApiResponse<Void> rejectProfileUpdate(@PathVariable Long id,
                                                 @RequestParam String reason) {

        csrservice.rejectUpdate(id, reason);
        return ApiResponse.ok("Customer profile update rejected", null);
    }

    @Operation(summary = "Get pending high-value transactions")
    @GetMapping("/pending-approval")
    public ApiResponse<List<TxnResponse>> pendingApprovals() {
        return ApiResponse.ok("Pending approvals fetched",
                transactionService.getPendingHighValueTxns());
    }

    @Operation(summary = "Approve high-value transaction")
    @PostMapping("/{txnId}/approvetxn")
    public ApiResponse<TxnResponse> approveTxn(@PathVariable UUID txnId) {
        return ApiResponse.ok("Transaction approved",
                transactionService.approveHighValueTxn(txnId));
    }

    @Operation(summary = "Reject high-value transaction")
    @PostMapping("/{txnId}/rejecttxn")
    public ApiResponse<TxnResponse> rejectTxn(@PathVariable UUID txnId,
                                              @RequestParam String reason) {
        return ApiResponse.ok("Transaction rejected",
                transactionService.rejectHighValueTxn(txnId, reason));
    }
}