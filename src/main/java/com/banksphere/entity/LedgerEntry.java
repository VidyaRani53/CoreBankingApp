package com.banksphere.entity;

import com.banksphere.entity.enums.LedgerEntryType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries",
        indexes = @Index(name = "idx_ledger_account_posted", columnList = "account_id, posted_at"))
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEntry {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false)
    private LedgerEntryType entryType;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "running_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal runningBalance;

    @Column(name = "posted_at", nullable = false)
    private Instant postedAt;

    @PrePersist
    void prePersist() {
        if (postedAt == null) postedAt = Instant.now();
    }
}