package com.banksphere.dto.account;

import com.banksphere.entity.enums.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class AccountApplicationSubmitRequest {

    @NotNull
    private UUID preferredBranchId;

    @NotNull
    private AccountType accountType;

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal initialDeposit;

    private String nomineeName;
    private String nomineeRelation;

    private String employmentType;
    private BigDecimal monthlyIncome;
    private String purpose;
}