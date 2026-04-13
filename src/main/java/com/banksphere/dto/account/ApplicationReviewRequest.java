package com.banksphere.dto.account;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApplicationReviewRequest {
    @NotBlank
    private String comment;
}