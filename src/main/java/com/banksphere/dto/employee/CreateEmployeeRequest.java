package com.banksphere.dto.employee;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateEmployeeRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Branch Code is required")
    private String branchCode; // System looks up branch by this code

    @NotBlank(message = "Designation is required")
    private String designation; // CSR or BRANCH_MANAGER
}