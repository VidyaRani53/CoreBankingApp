package com.banksphere.controller;

import com.banksphere.dto.ApiResponse;
import com.banksphere.dto.branch.BranchResponse;
import com.banksphere.dto.branch.CreateBranchRequest;
import com.banksphere.dto.branch.UpdateBranchStatusRequest;
import com.banksphere.service.BranchService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @Operation(summary = "Create branch")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public ApiResponse<BranchResponse> create(@Valid @RequestBody CreateBranchRequest request) {
        BranchResponse created = branchService.createBranch(request);
        return ApiResponse.ok("Branch created successfully", created);
    }

    @Operation(summary = "Get all branches")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    public ApiResponse<List<BranchResponse>> list() {
        List<BranchResponse> branches = branchService.getAllBranches();
        return ApiResponse.ok("Branches fetched successfully", branches);
    }

    @Operation(summary = "Update branch status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PatchMapping("/{id}/status")
    public ApiResponse<BranchResponse> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateBranchStatusRequest request
    ) {
        BranchResponse updated = branchService.updateBranchStatus(id, request.getStatus());
        return ApiResponse.ok("Branch status updated successfully", updated);
    }
}