package com.banksphere.dto.txn;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class WithdrawRequest {
    @NotNull private UUID accountId;
    @NotBlank private String txnRef;
    @NotNull @DecimalMin("0.01") private BigDecimal amount;
    private String narration;
}