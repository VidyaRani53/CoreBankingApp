package com.banksphere.repository;

import com.banksphere.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    Optional<Employee> findByUserId(UUID userId);
    long countByBranchId(java.util.UUID branchId);
}