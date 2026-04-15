package com.banksphere.controller;

import com.banksphere.dto.ApiResponse;
import com.banksphere.dto.customer.CustomerProfileUpdateRequest;
import com.banksphere.dto.customer.CustomerResponse;
import com.banksphere.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @GetMapping("/me")
    public ApiResponse<CustomerResponse> me() {
        return ApiResponse.ok("Customer profile fetched", customerService.getMyProfile());
    }

    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @PostMapping("/profile-update")
    public ApiResponse<CustomerResponse> requestUpdate(
            @RequestBody CustomerProfileUpdateRequest request) {

        customerService.requestProfileUpdate(request);
        return ApiResponse.ok("Profile update request submitted for approval",customerService.getMyProfile());
    }





}