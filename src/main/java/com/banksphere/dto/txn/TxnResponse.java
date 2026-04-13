package com.banksphere.dto.txn;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class TxnResponse {
    private UUID id;
    private String txnRef;
    private String txnType;
    private String status;
    private BigDecimal amount;
    private UUID fromAccountId;
    private UUID toAccountId;
    private Instant requestedAt;
    private Instant postedAt;
    private String failureReason;
}