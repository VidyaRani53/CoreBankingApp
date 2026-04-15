package com.banksphere.repository;

import com.banksphere.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByUserId(UUID userId);


    Optional<Customer> findByCustomerNo(String customerNo);

    Optional<Customer> findByCustomerNoAndStatus(String customerNo, String status);

    List<Customer> findByBranchId(UUID branchId);

    List<Customer> findByBranchIdAndStatus(UUID branchId, String status);


}