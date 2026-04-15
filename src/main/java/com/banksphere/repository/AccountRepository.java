package com.banksphere.repository;

import com.banksphere.entity.Account;
import com.banksphere.entity.enums.AccountStatus;
import com.banksphere.entity.enums.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    // Existing
    List<Account> findByCustomerIdOrderByOpenedAtDesc(UUID customerId);

    Optional<Account> findByAccountNo(String accountNo);

    // ✅ Branch based
    List<Account> findByBranchCode(String branchCode);

    List<Account> findByBranchCodeAndStatus(
            String branchCode,
            AccountStatus status
    );
    List<Account> findByBranchCodeAndAccountType(
            String branchCode,
            AccountType accountType
    );
    List<Account> findByBranchCodeAndStatusAndAccountType(
            String branchCode,
            AccountStatus status,
            AccountType accountType
    );
}