package com.banksphere.dto.txn;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;
@Data
public class TransferRequest {
    @NotNull private UUID fromAccountId;
    @NotNull private UUID toAccountId;
    @NotBlank private String txnRef;
    @NotNull @DecimalMin("0.01") private BigDecimal amount;
    private String narration;
}