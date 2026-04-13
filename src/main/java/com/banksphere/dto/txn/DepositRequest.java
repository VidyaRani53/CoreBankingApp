package com.banksphere.dto.txn;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class DepositRequest {
    @NotBlank(message = "Account number is required")
    private String accountNo; // e.g., BR0001-1001

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    private String narration;
}