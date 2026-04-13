package com.banksphere.dto.employee;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CreateEmployeeResponse {
    private UUID userId;
    private UUID employeeId;
    private String username;
    private String tempPassword; // show only once
    private String roleAssigned; // ROLE_CSR or ROLE_BRANCH_MANAGER
    private UUID branchId;
}