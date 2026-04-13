package com.banksphere.dto.txn;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferRequest {

    @NotBlank(message = "Source account number is required")
    private String fromAccountNo;

    @NotBlank(message = "Destination account number is required")
    private String toAccountNo;

    @NotNull(message = "Transfer amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    private BigDecimal amount;

    private String narration;
}