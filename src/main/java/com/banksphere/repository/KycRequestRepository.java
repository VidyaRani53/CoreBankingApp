package com.banksphere.repository;

import com.banksphere.entity.KycRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface KycRequestRepository extends JpaRepository<KycRequest, UUID> {

    List<KycRequest> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    List<KycRequest> findByBranchIdAndStatusInOrderByCreatedAtDesc(UUID branchId, List<String> statuses);
}