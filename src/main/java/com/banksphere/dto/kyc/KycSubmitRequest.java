package com.banksphere.dto.kyc;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class KycSubmitRequest {

    @NotBlank
    private String idType;   // AADHAR / PAN / PASSPORT / DL

    @NotBlank
    private String idNumber;

    private String addressLine1;
    private String city;
    private String state;
    private String pincode;
    private String country; // optional, default India
}