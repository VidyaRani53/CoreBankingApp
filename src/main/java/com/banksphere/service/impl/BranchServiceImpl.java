package com.banksphere.service.impl;

import com.banksphere.dto.branch.BranchResponse;
import com.banksphere.dto.branch.CreateBranchRequest;
import com.banksphere.entity.Branch;
import com.banksphere.repository.BranchRepository;
import com.banksphere.service.BranchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;

    @Override
    @Transactional
    public BranchResponse createBranch(CreateBranchRequest request) {
        // 1. Generate Automatic Branch Code (e.g., BR0001, BR0002)
        long nextId = branchRepository.count() + 1;
        String generatedBranchCode = String.format("BR%04d", nextId);

        // 2. Generate Automatic IFSC (Bank Name + 0 + Branch Sequence)
        // Standard IFSC is 11 characters: BKSP (4) + 0 (1) + 000001 (6)
        String generatedIfsc = String.format("BKSP0%06d", nextId);

        // 3. Build Entity
        Branch branch = Branch.builder()
                .branchCode(generatedBranchCode)
                .ifsc(generatedIfsc)
                .name(request.getName().trim())
                .addressLine1(request.getAddressLine1().trim())
                .city(request.getCity().trim())
                .state(request.getState().trim())
                .pincode(request.getPincode().trim())
                .status("ACTIVE")
                .build();

        Branch saved = branchRepository.save(branch);
        log.info("New Branch Auto-Generated: ID={}, Code={}, IFSC={}",
                saved.getId(), saved.getBranchCode(), saved.getIfsc());

        return toResponse(saved);
    }

    @Override
    public List<BranchResponse> getAllBranches() {
        return branchRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public BranchResponse updateBranchStatus(String branchId, String status) {
        UUID id;
        try {
            id = UUID.fromString(branchId);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid branchId format: " + branchId);
        }

        String newStatus = (status == null) ? "" : status.trim().toUpperCase();
        if (!newStatus.equals("ACTIVE") && !newStatus.equals("INACTIVE")) {
            throw new IllegalArgumentException("Status must be ACTIVE or INACTIVE");
        }

        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found with ID: " + branchId));

        branch.setStatus(newStatus);
        Branch saved = branchRepository.save(branch);

        log.info("Branch status updated: {} -> {}", saved.getBranchCode(), saved.getStatus());
        return toResponse(saved);
    }

    private BranchResponse toResponse(Branch b) {
        return BranchResponse.builder()
                .id(b.getId())
                .branchCode(b.getBranchCode())
                .name(b.getName())
                .ifsc(b.getIfsc())
                .addressLine1(b.getAddressLine1())
                .city(b.getCity())
                .state(b.getState())
                .pincode(b.getPincode())
                .status(b.getStatus())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}