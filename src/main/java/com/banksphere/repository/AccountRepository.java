package com.banksphere.repository;

import com.banksphere.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    List<Account> findByCustomerIdOrderByOpenedAtDesc(UUID customerId);
    Optional<Account> findByAccountNo(String accountNo);
}