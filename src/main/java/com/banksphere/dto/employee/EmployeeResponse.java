package com.banksphere.dto.employee;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class EmployeeResponse {
    private UUID employeeId;
    private UUID userId;
    private String username;
    private String employeeNo;
    private String designation;
    private String status;
    private UUID branchId;
}