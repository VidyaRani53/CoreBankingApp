package com.banksphere.service.impl;

import com.banksphere.dto.branch.BranchResponse;
import com.banksphere.dto.branch.CreateBranchRequest;
import com.banksphere.entity.Branch;
import com.banksphere.repository.BranchRepository;
import com.banksphere.service.BranchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;

    @Override
    public BranchResponse createBranch(CreateBranchRequest request) {
        String branchCode = request.getBranchCode().trim();

        if (branchRepository.existsByBranchCode(branchCode)) {
            throw new IllegalArgumentException("Branch code already exists: " + branchCode);
        }

        String ifsc = request.getIfsc();
        if (ifsc != null && !ifsc.isBlank()) {
            ifsc = ifsc.trim();
            if (branchRepository.existsByIfsc(ifsc)) {
                throw new IllegalArgumentException("IFSC already exists: " + ifsc);
            }
        } else {
            ifsc = null;
        }

        Branch branch = Branch.builder()
                .branchCode(branchCode)
                .name(request.getName().trim())
                .ifsc(ifsc)
                .addressLine1(request.getAddressLine1().trim())
                .city(request.getCity().trim())
                .state(request.getState().trim())
                .pincode(request.getPincode().trim())
                .status("ACTIVE")
                .build();

        Branch saved = branchRepository.save(branch);
        log.info("Branch created: id={}, branchCode={}", saved.getId(), saved.getBranchCode());

        return toResponse(saved);
    }

    @Override
    public List<BranchResponse> getAllBranches() {
        return branchRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
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
    @Override
    public BranchResponse updateBranchStatus(String branchId, String status) {
        UUID id;
        try {
            id = UUID.fromString(branchId);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid branchId UUID: " + branchId);
        }

        String newStatus = status == null ? "" : status.trim().toUpperCase();
        if (!newStatus.equals("ACTIVE") && !newStatus.equals("INACTIVE")) {
            throw new IllegalArgumentException("Status must be ACTIVE or INACTIVE");
        }

        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found: " + branchId));

        branch.setStatus(newStatus);
        Branch saved = branchRepository.save(branch);

        log.info("Branch status updated: id={}, status={}", saved.getId(), saved.getStatus());
        return toResponse(saved);
    }
}