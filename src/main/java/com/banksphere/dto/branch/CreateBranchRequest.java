package com.banksphere.dto.branch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateBranchRequest {

    @NotBlank(message = "Branch name is required")
    @Size(max = 120)
    private String name;

    @NotBlank(message = "Address is required")
    @Size(max = 255)
    private String addressLine1;

    @NotBlank(message = "City is required")
    @Size(max = 80)
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 80)
    private String state;

    @NotBlank(message = "Pincode is required")
    @Size(max = 12)
    private String pincode;
}