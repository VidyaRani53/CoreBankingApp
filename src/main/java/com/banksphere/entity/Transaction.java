package com.banksphere.entity;

import com.banksphere.entity.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions",
        uniqueConstraints = @UniqueConstraint(name = "uk_transactions_txn_ref", columnNames = "txn_ref"))
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "txn_ref", nullable = false, length = 50)
    private String txnRef;

    @Enumerated(EnumType.STRING)
    @Column(name = "txn_type", nullable = false)
    private TxnType txnType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TxnStatus status;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "from_account_id")
    private UUID fromAccountId;

    @Column(name = "to_account_id")
    private UUID toAccountId;

    @Column(name = "initiated_by", nullable = false)
    private UUID initiatedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private TxnChannel channel;

    @Column(name = "narration")
    private String narration;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "posted_at")
    private Instant postedAt;

    @Column(name = "failure_reason")
    private String failureReason;

    @PrePersist
    void prePersist() {
        if (requestedAt == null) requestedAt = Instant.now();
        if (status == null) status = TxnStatus.PENDING;
        if (currency == null) currency = "INR";
        if (channel == null) channel = TxnChannel.PORTAL;
    }
}