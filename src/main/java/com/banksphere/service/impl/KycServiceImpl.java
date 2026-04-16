package com.banksphere.service.impl;

import com.banksphere.dto.kyc.KycResponse;
import com.banksphere.dto.kyc.KycReviewRequest;
import com.banksphere.dto.kyc.KycSubmitRequest;
import com.banksphere.entity.*;
import com.banksphere.repository.*;
import com.banksphere.service.KycService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KycServiceImpl implements KycService {

    private final KycRequestRepository kycRequestRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final BranchRepository branchRepository;

    @Override
    @Transactional
    public KycResponse submit(KycSubmitRequest request) {
        User currentUser = getCurrentUser();

        Customer customer = customerRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Customer profile not found for current user"));

        // Use the customer's linked branch
        UUID branchId = customer.getBranchId();

        KycRequest kyc = KycRequest.builder()
                .customerId(customer.getId())
                .branchId(branchId)
                .status("SUBMITTED")
                .idType(request.getIdType().trim().toUpperCase())
                .idNumber(request.getIdNumber().trim())
                .addressLine1(request.getAddressLine1())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .country(request.getCountry() != null ? request.getCountry() : "India")
                .build();

        KycRequest saved = kycRequestRepository.save(kyc);

        // Sync customer KYC status
        customer.setKycStatus("PENDING");
        customerRepository.save(customer);

        return toResponse(saved);
    }

    @Override
    public List<KycResponse> myHistory() {
        User currentUser = getCurrentUser();

        Customer customer = customerRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Customer profile not found for current user"));

        return kycRequestRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId())
                .stream().map(this::toResponse).toList();
    }

    @Override
    public List<KycResponse> inbox() {
        User currentUser = getCurrentUser();

        Employee employee = employeeRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Employee profile not found for current user"));

        List<String> statuses = List.of("SUBMITTED", "IN_REVIEW");

        return kycRequestRepository.findByBranchIdAndStatusInOrderByCreatedAtDesc(employee.getBranchId(), statuses)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public KycResponse review(Long kycRequestId, KycReviewRequest request) {
        User currentUser = getCurrentUser();

        Employee employee = employeeRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Employee profile not found for current user"));

        KycRequest kyc = kycRequestRepository.findById(kycRequestId)
                .orElseThrow(() -> new RuntimeException("KYC request not found: " + kycRequestId));

        if (!kyc.getBranchId().equals(employee.getBranchId())) {
            throw new RuntimeException("Access Denied: You cannot review KYC for another branch");
        }

        String decision = request.getDecision().trim().toUpperCase();
        if (!decision.equals("APPROVE") && !decision.equals("REJECT")) {
            throw new RuntimeException("Decision must be APPROVE or REJECT");
        }

        kyc.setStatus(decision.equals("APPROVE") ? "APPROVED" : "REJECTED");
        kyc.setReviewedByUserId(currentUser.getId());
        kyc.setReviewComment(request.getComment());
        kyc.setReviewedAt(Instant.now());

        KycRequest saved = kycRequestRepository.save(kyc);

        // Sync customer's master KYC status
        Customer customer = customerRepository.findById(kyc.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found for KYC request"));

        customer.setKycStatus(kyc.getStatus());
        customerRepository.save(customer);

        return toResponse(saved);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    /**
     * The logic to convert UUIDs into readable business codes (Branch Code & Customer No)
     */
    private KycResponse toResponse(KycRequest k) {
        // Fetch Branch Code from Branch Repository
        String branchCode = branchRepository.findById(k.getBranchId())
                .map(Branch::getBranchCode)
                .orElse("UNKNOWN");

        // Fetch Customer No from Customer Repository
        String customerNo = customerRepository.findById(k.getCustomerId())
                .map(Customer::getCustomerNo)
                .orElse("UNKNOWN");

        return KycResponse.builder()
                .id(k.getId())
                .customerNo(customerNo)   // Maps to customerNo
                .branchCode(branchCode)   // Maps to branchCode
                .status(k.getStatus())
                .idType(k.getIdType())
                .idNumber(k.getIdNumber())
                .addressLine1(k.getAddressLine1())
                .city(k.getCity())
                .state(k.getState())
                .pincode(k.getPincode())
                .country(k.getCountry())
                .reviewedByUserId(k.getReviewedByUserId())
                .reviewComment(k.getReviewComment())
                .reviewedAt(k.getReviewedAt())
                .createdAt(k.getCreatedAt())
                .updatedAt(k.getUpdatedAt())
                .build();
    }
}