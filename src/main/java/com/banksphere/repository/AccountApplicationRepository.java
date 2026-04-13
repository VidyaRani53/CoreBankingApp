package com.banksphere.repository;

import com.banksphere.entity.AccountApplication;
import com.banksphere.entity.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccountApplicationRepository extends JpaRepository<AccountApplication, UUID> {

    List<AccountApplication> findByCustomerIdOrderBySubmittedAtDesc(UUID customerId);

    List<AccountApplication> findByPreferredBranchIdAndStatusOrderBySubmittedAtDesc(UUID preferredBranchId,
                                                                                    ApplicationStatus status);
}