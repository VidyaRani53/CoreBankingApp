package com.banksphere.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterRequest {

    @NotBlank
    private String username;

    @NotBlank
    @Size(min = 6, message = "password must be at least 6 characters")
    private String password;

    @NotBlank
    private String fullName;

    @Email
    private String email;

    private String phone;

    private LocalDate dob;

    private String gender; // MALE/FEMALE/OTHER

    @NotBlank
    private String branchCode; // e.g. HYD001
}