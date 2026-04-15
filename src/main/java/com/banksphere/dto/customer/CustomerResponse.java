package com.banksphere.dto.customer;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class CustomerResponse {

    private UUID id;
    private String customerNo;
    private String fullName;
    private LocalDate dob;
    private String gender;

    private String email;
    private String phone;

    private String addressLine1;
    private String city;
    private String state;
    private String pincode;
    private String country;

    private String kycStatus;

    private Instant createdAt;
    private Instant updatedAt;
    private String status;

}