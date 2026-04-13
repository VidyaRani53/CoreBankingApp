package com.banksphere.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "kyc_requests", indexes = {
        @Index(name = "idx_kyc_customer_id", columnList = "customer_id"),
        @Index(name = "idx_kyc_branch_id", columnList = "branch_id"),
        @Index(name = "idx_kyc_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycRequest {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    // Which branch will process it (chosen by system/admin at submission time)
    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(nullable = false, length = 20)
    private String status; // SUBMITTED / IN_REVIEW / APPROVED / REJECTED

    // Submitted data (keep simple for now; later we can add document table)
    @Column(nullable = false, length = 30)
    private String idType; // AADHAR / PAN / PASSPORT / DL

    @Column(nullable = false, length = 60)
    private String idNumber;

    private String addressLine1;
    private String city;
    private String state;
    private String pincode;
    private String country;

    // Review fields
    @Column(name = "reviewed_by_user_id")
    private UUID reviewedByUserId;

    private String reviewComment;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) this.status = "SUBMITTED";
        if (this.country == null) this.country = "India";
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}