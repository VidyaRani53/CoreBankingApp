package com.banksphere.service.impl;

import com.banksphere.dto.customer.CustomerProfileUpdateRequest;
import com.banksphere.dto.customer.CustomerResponse;
import com.banksphere.entity.Customer;
import com.banksphere.entity.CustomerUpdateRequest;
import com.banksphere.entity.User;
import com.banksphere.repository.CustomerRepository;
import com.banksphere.repository.CustomerUpdateRequestRepository;
import com.banksphere.repository.UserRepository;
import com.banksphere.service.CustomerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final CustomerUpdateRequestRepository updateRequestRepository;
    @Override
    public CustomerResponse getMyProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName(); // this comes from UserDetails username

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found for username: " + username));

        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Customer profile not found for userId: " + user.getId()));

        return toResponse(customer);
    }

    @Transactional
    public void requestProfileUpdate(CustomerProfileUpdateRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName(); // ✅ same as getMyProfile()

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found for username: " + username));

        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Customer profile not found for userId: " + user.getId()));

        CustomerUpdateRequest upd = CustomerUpdateRequest.builder()
                .customerId(customer.getId())
                .fullName(request.getFullName())
                .dob(request.getDob())
                .gender(request.getGender())
                .email(request.getEmail())
                .phone(request.getPhone())
                .addressLine1(request.getAddressLine1())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .build();

        updateRequestRepository.save(upd);
    }





    private CustomerResponse toResponse(Customer c) {
        return CustomerResponse.builder()
                .id(c.getId())
                .customerNo(c.getCustomerNo())
                .fullName(c.getFullName())
                .dob(c.getDob())
                .gender(c.getGender())
                .email(c.getEmail())
                .phone(c.getPhone())
                .addressLine1(c.getAddressLine1())
                .city(c.getCity())
                .state(c.getState())
                .pincode(c.getPincode())
                .country(c.getCountry())
                .kycStatus(c.getKycStatus())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }


}