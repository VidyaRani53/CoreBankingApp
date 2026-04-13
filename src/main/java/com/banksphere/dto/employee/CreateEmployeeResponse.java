package com.banksphere.dto.employee;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class CreateEmployeeResponse {
    private UUID userId;
    private UUID employeeId;     // Internal Database ID
    private String employeeNo;   // Business ID (e.g., BR0001-001)
    private String username;
    private String tempPassword; // Shown only once
    private String roleAssigned; // ROLE_CSR or ROLE_BRANCH_MANAGER
    private UUID branchId;
}