package com.banksphere.dto.employee;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResetPasswordResponse {
    private String username;
    private String tempPassword;
}