package com.banksphere.controller;

import com.banksphere.dto.ApiResponse;
import com.banksphere.dto.customer.CustomerResponse;
import com.banksphere.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}