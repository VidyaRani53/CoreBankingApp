package com.banksphere.controller;

import com.banksphere.dto.ApiResponse;
import com.banksphere.dto.employee.CreateEmployeeRequest;
import com.banksphere.dto.employee.CreateEmployeeResponse;
import com.banksphere.dto.employee.EmployeeResponse;
import com.banksphere.dto.employee.ResetPasswordResponse;
import com.banksphere.service.AdminEmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/employees")
@RequiredArgsConstructor
public class AdminEmployeeController {

    private final AdminEmployeeService adminEmployeeService;

    @Operation(summary = "Create employee")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public ApiResponse<CreateEmployeeResponse> create(@Valid @RequestBody CreateEmployeeRequest request) {
        return ApiResponse.ok("Employee created", adminEmployeeService.createEmployee(request));
    }

    @Operation(summary = "Get all employees")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    public ApiResponse<List<EmployeeResponse>> getAll() {
        return ApiResponse.ok("Employees fetched", adminEmployeeService.getAllEmployees());
    }

    @Operation(summary = "Reset employee password")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/{username}/reset-password")
    public ApiResponse<ResetPasswordResponse> resetPassword(@PathVariable String username) {
        return ApiResponse.ok("Password reset", adminEmployeeService.resetPassword(username));
    }
}