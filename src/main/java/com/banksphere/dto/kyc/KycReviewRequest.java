package com.banksphere.dto.kyc;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class KycReviewRequest {

    @NotBlank
    private String decision; // APPROVE / REJECT

    private String comment;
}