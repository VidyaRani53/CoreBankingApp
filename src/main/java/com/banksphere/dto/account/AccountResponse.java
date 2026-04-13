package com.banksphere.dto.account;

import com.banksphere.entity.enums.AccountType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class AccountResponse {
    private UUID id;
    private String accountNo;
    private String customerNo;  // Changed from UUID customerId
    private String branchCode;  // Changed from UUID branchId
    private AccountType accountType;
    private String status;
    private String currency;
    private BigDecimal availableBalance;
    private Instant openedAt;
}