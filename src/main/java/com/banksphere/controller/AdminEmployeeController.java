package com.banksphere.controller;

import com.banksphere.dto.ApiResponse;
import com.banksphere.dto.employee.CreateEmployeeRequest;
import com.banksphere.dto.employee.CreateEmployeeResponse;
import com.banksphere.service.AdminEmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.banksphere.dto.employee.EmployeeResponse;
import java.util.List;
import com.banksphere.dto.employee.ResetPasswordResponse;
@RestController
@RequestMapping("/api/admin/employees")
@RequiredArgsConstructor
public class AdminEmployeeController {

    private final AdminEmployeeService adminEmployeeService;

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public ApiResponse<CreateEmployeeResponse> create(@Valid @RequestBody CreateEmployeeRequest request) {
        return ApiResponse.ok("Employee created", adminEmployeeService.createEmployee(request));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    public ApiResponse<List<EmployeeResponse>> getAll() {
        return ApiResponse.ok("Employees fetched", adminEmployeeService.getAllEmployees());
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/{username}/reset-password")
    public ApiResponse<ResetPasswordResponse> resetPassword(@PathVariable String username) {
        return ApiResponse.ok("Password reset", adminEmployeeService.resetPassword(username));
    }
}