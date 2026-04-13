package com.banksphere.dto.branch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateBranchRequest {

    @NotBlank
    @Size(max = 20)
    private String branchCode;

    @NotBlank
    @Size(max = 120)
    private String name;

    @Size(max = 20)
    private String ifsc; // optional

    @NotBlank
    @Size(max = 255)
    private String addressLine1;

    @NotBlank
    @Size(max = 80)
    private String city;

    @NotBlank
    @Size(max = 80)
    private String state;

    @NotBlank
    @Size(max = 12)
    private String pincode;
}