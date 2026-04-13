package com.banksphere.dto.kyc;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class KycResponse {
    private UUID id;
    private String customerNo; // Changed from UUID customerId
    private String branchCode; // Changed from UUID branchId
    private String status;

    private String idType;
    private String idNumber;

    private String addressLine1;
    private String city;
    private String state;
    private String pincode;
    private String country;

    private UUID reviewedByUserId;
    private String reviewComment;
    private Instant reviewedAt;

    private Instant createdAt;
    private Instant updatedAt;
}