package com.banksphere.dto.account;

import com.banksphere.entity.enums.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountApplicationSubmitRequest {

    @NotBlank(message = "Preferred branch code is required")
    private String preferredBranchCode; // Changed from UUID preferredBranchId

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @NotNull
    @DecimalMin(value = "0.00", message = "Initial deposit cannot be negative")
    private BigDecimal initialDeposit;

    private String nomineeName;
    private String nomineeRelation;

    private String employmentType;
    private BigDecimal monthlyIncome;
    private String purpose;
}