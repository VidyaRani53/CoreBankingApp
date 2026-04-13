package com.banksphere.repository;

import com.banksphere.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BranchRepository extends JpaRepository<Branch, UUID> {

    Optional<Branch> findByBranchCode(String branchCode);

    Optional<Branch> findByIfsc(String ifsc);

    boolean existsByBranchCode(String branchCode);

    boolean existsByIfsc(String ifsc);
}