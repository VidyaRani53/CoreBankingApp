package com.banksphere.controller;

import com.banksphere.dto.ApiResponse;
import com.banksphere.dto.branch.BranchResponse;
import com.banksphere.dto.branch.CreateBranchRequest;
import com.banksphere.service.BranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.banksphere.dto.branch.UpdateBranchStatusRequest;
import java.util.List;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public ApiResponse<BranchResponse> create(@Valid @RequestBody CreateBranchRequest request) {
        BranchResponse created = branchService.createBranch(request);
        return ApiResponse.ok("Branch created", created);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    public ApiResponse<List<BranchResponse>> list() {
        return ApiResponse.ok("Branches fetched", branchService.getAllBranches());
    }
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PatchMapping("/{id}/status")
    public ApiResponse<BranchResponse> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateBranchStatusRequest request
    ) {
        BranchResponse updated = branchService.updateBranchStatus(id, request.getStatus());
        return ApiResponse.ok("Branch status updated", updated);
    }
}