package com.banksphere.dto.branch;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateBranchStatusRequest {

    @NotBlank
    private String status; // ACTIVE / INACTIVE
}