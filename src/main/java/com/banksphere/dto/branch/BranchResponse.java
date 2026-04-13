package com.banksphere.dto.branch;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class BranchResponse {
    private UUID id;
    private String branchCode;
    private String name;
    private String ifsc;
    private String addressLine1;
    private String city;
    private String state;
    private String pincode;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}