package com.banksphere.entity;

import com.banksphere.entity.enums.AccountStatus;
import com.banksphere.entity.enums.AccountType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_accounts_account_no", columnNames = "account_no")
        })
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "account_no", nullable = false, length = 30)
    private String accountNo;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccountStatus status;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "available_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal availableBalance;

    @Column(name = "opened_at", nullable = false)
    private Instant openedAt;

    @Version
    @Column(name = "version", nullable = false)
    private int version;

    @PrePersist
    void prePersist() {
        if (status == null) status = AccountStatus.ACTIVE;
        if (currency == null) currency = "INR";
        if (availableBalance == null) availableBalance = BigDecimal.ZERO;
        if (openedAt == null) openedAt = Instant.now();
    }
}