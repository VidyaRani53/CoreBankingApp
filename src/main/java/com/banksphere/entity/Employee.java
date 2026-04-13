package com.banksphere.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "employees",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_employees_user_id", columnNames = "user_id"),
                @UniqueConstraint(name = "uk_employees_employee_no", columnNames = "employee_no")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId; // FK users.id

    @Column(name = "branch_id", nullable = false)
    private UUID branchId; // FK branches.id

    @Column(name = "employee_no", nullable = false, unique = true, length = 30)
    private String employeeNo;

    @Column(nullable = false, length = 30)
    private String designation; // CSR / BRANCH_MANAGER

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