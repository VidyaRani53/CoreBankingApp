package com.banksphere.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "branches",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_branches_branch_code", columnNames = "branch_code"),
                @UniqueConstraint(name = "uk_branches_ifsc", columnNames = "ifsc")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Branch {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "branch_code", nullable = false, length = 20)
    private String branchCode;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(unique = true, length = 20)
    private String ifsc; // optional

    @Column(name = "address_line1", nullable = false, length = 255)
    private String addressLine1;

    @Column(nullable = false, length = 80)
    private String city;

    @Column(nullable = false, length = 80)
    private String state;

    @Column(nullable = false, length = 12)
    private String pincode;

    @Column(nullable = false, length = 20)
    private String status; // ACTIVE / INACTIVE

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) this.status = "ACTIVE";
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}