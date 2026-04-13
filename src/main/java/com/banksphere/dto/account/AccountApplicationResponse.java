package com.banksphere.dto.account;

import com.banksphere.entity.enums.AccountType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class AccountApplicationResponse {
    private UUID id;
    private UUID customerId;
    private UUID preferredBranchId;
    private AccountType accountType;
    private BigDecimal initialDeposit;
    private String status;
    private Instant submittedAt;

    private UUID reviewedByUserId;
    private Instant reviewedAt;
    private String remarks;
}