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


    List<Account> findByCustomerIdAndStatus(UUID customerId, AccountStatus status);

    // ✅ For admin checks
    Optional<Account> findByAccountNoAndStatusNot(String accountNo, AccountStatus status);


    Optional<Account> findById(UUID id);

    Optional<Account> findByIdAndCustomerId(UUID id, UUID customerId);

    Optional<Account> findByIdAndBranchCode(UUID id, String branchCode);

    // ✅ All accounts of a customer
    List<Account> findByCustomerId(UUID customerId);

    // ✅ Branch-scoped (for Branch Manager)
    List<Account> findByCustomerIdAndBranchCode(
            UUID customerId,
            String branchCode
    );


}