package com.banksphere.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "customers",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_customers_user_id", columnNames = "user_id"),
                @UniqueConstraint(name = "uk_customers_customer_no", columnNames = "customer_no"),
                @UniqueConstraint(name = "uk_customers_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_customers_phone", columnNames = "phone")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId; // FK to users.id (interim: store as UUID only)

    @Column(name = "customer_no", nullable = false, unique = true, length = 30)
    private String customerNo;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    private LocalDate dob;

    @Column(length = 10)
    private String gender; // MALE / FEMALE / OTHER

    @Column(length = 120, unique = true)
    private String email;

    @Column(length = 20, unique = true)
    private String phone;

    @Column(name = "address_line1", length = 255)
    private String addressLine1;

    @Column(length = 80)
    private String city;

    @Column(length = 80)
    private String state;

    @Column(length = 12)
    private String pincode;

    @Column(length = 80)
    private String country;

    @Column(name = "kyc_status", nullable = false, length = 20)
    private String kycStatus; // PENDING / SUBMITTED / VERIFIED / REJECTED

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.kycStatus == null) this.kycStatus = "PENDING";
        if (this.country == null) this.country = "India";
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}