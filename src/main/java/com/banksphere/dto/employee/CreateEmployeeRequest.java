package com.banksphere.dto.employee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateEmployeeRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String employeeNo;

    @NotNull
    private UUID branchId;

    @NotBlank
    private String designation; // CSR / BRANCH_MANAGER
}