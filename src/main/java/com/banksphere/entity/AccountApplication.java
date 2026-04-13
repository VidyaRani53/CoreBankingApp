package com.banksphere.entity;

import com.banksphere.entity.enums.AccountType;
import com.banksphere.entity.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "account_applications")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountApplication {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "preferred_branch_id", nullable = false)
    private UUID preferredBranchId;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Column(name = "initial_deposit", nullable = false, precision = 19, scale = 2)
    private BigDecimal initialDeposit;

    @Column(name = "nominee_name")
    private String nomineeName;

    @Column(name = "nominee_relation")
    private String nomineeRelation;

    @Column(name = "employment_type")
    private String employmentType; // keep string for interim

    @Column(name = "monthly_income", precision = 19, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(name = "purpose")
    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApplicationStatus status;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "reviewed_by_user_id")
    private UUID reviewedByUserId;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "remarks")
    private String remarks;

    @PrePersist
    void prePersist() {
        if (status == null) status = ApplicationStatus.SUBMITTED;
        if (submittedAt == null) submittedAt = Instant.now();
        if (initialDeposit == null) initialDeposit = BigDecimal.ZERO;
    }
}