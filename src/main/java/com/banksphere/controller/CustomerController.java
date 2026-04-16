package com.banksphere.controller;

import com.banksphere.dto.ApiResponse;
import com.banksphere.dto.customer.CustomerProfileUpdateRequest;
import com.banksphere.dto.customer.CustomerResponse;
import com.banksphere.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "Get my customer profile")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @GetMapping("/me")
    public ApiResponse<CustomerResponse> me() {
        return ApiResponse.ok("Customer profile fetched", customerService.getMyProfile());
    }

    @Operation(summary = "Request profile update")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @PostMapping("/profile-update")
    public ApiResponse<CustomerResponse> requestUpdate(@RequestBody CustomerProfileUpdateRequest request) {
        customerService.requestProfileUpdate(request);
        return ApiResponse.ok("Profile update request submitted for approval", customerService.getMyProfile());
    }

    @Operation(summary = "Deactivate customer")
    @PatchMapping("/{customerNo}/deactivate")
    @PreAuthorize("hasAnyAuthority('ROLE_CSR','ROLE_BRANCH_MANAGER')")
    public ApiResponse<CustomerResponse> deactivateCustomer(@PathVariable String customerNo) {
        return ApiResponse.ok(
                "Customer deactivated successfully",
                customerService.deactivateCustomer(customerNo)
        );
    }

    @Operation(summary = "Get my branch customers")
    @GetMapping("/my-branch")
    @PreAuthorize("hasAnyAuthority('ROLE_CSR','ROLE_BRANCH_MANAGER')")
    public ApiResponse<List<CustomerResponse>> getCustomersInMyBranch(@RequestParam(required = false) String status) {
        return ApiResponse.ok(
                "Branch customers fetched successfully",
                customerService.getCustomersInMyBranch(status)
        );
    }

    @Operation(summary = "Reactivate customer")
    @PatchMapping("/{customerNo}/reactivate")
    @PreAuthorize("hasAnyAuthority('ROLE_CSR','ROLE_BRANCH_MANAGER')")
    public ApiResponse<CustomerResponse> reactivateCustomer(@PathVariable String customerNo) {
        return ApiResponse.ok(
                "Customer reactivated successfully",
                customerService.reactivateCustomer(customerNo)
        );
    }
}