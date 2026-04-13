package com.banksphere.service;

import com.banksphere.dto.branch.BranchResponse;
import com.banksphere.dto.branch.CreateBranchRequest;

import java.util.List;

public interface BranchService {

    BranchResponse createBranch(CreateBranchRequest request);

    List<BranchResponse> getAllBranches();
    BranchResponse updateBranchStatus(String branchId, String status);
}