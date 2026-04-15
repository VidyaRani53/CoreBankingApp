package com.banksphere.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "customer_update_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerUpdateRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID customerId;

    @Column(length = 150)
    private String fullName;

    private LocalDate dob;

    @Column(length = 10)
    private String gender;

    @Column(length = 120)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String addressLine1;

    @Column(length = 80)
    private String city;

    @Column(length = 80)
    private String state;

    @Column(length = 12)
    private String pincode;

    @Column(nullable = false, length = 20)
    private String status; // PENDING, APPROVED, REJECTED

    private String rejectionReason;

    private Instant createdAt;
    private Instant processedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
        this.status = "PENDING";
    }
}
