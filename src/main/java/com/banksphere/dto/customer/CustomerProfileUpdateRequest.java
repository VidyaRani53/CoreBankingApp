package com.banksphere.dto.customer;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CustomerProfileUpdateRequest {

    private String fullName;
    private LocalDate dob;
    private String gender;
    private String email;
    private String phone;
    private String addressLine1;
    private String city;
    private String state;
    private String pincode;
}
