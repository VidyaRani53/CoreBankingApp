package com.banksphere.service.impl;

import com.banksphere.dto.kyc.KycResponse;
import com.banksphere.dto.kyc.KycReviewRequest;
import com.banksphere.dto.kyc.KycSubmitRequest;
import com.banksphere.entity.*;
import com.banksphere.repository.*;
import com.banksphere.service.KycService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
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

        KycRequest kyc = KycRequest.builder()
                .customerId(customer.getId())
                .branchId(customer.getBranchId())
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

        customer.setKycStatus("PENDING");
        customerRepository.save(customer);

        log.info("KYC submitted for Customer: {}", customer.getCustomerNo());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KycResponse> myHistory() {
        User currentUser = getCurrentUser();
        Customer customer = customerRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Customer profile not found"));

        return kycRequestRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId())
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<KycResponse> inbox() {
        User currentUser = getCurrentUser();
        Employee employee = employeeRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Employee profile not found"));

        List<String> statuses = List.of("SUBMITTED", "IN_REVIEW");

        return kycRequestRepository.findByBranchIdAndStatusInOrderByCreatedAtDesc(employee.getBranchId(), statuses)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public KycResponse review(UUID kycRequestId, KycReviewRequest request) {
        User currentUser = getCurrentUser();
        Employee employee = employeeRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Employee profile not found"));

        KycRequest kyc = kycRequestRepository.findById(kycRequestId)
                .orElseThrow(() -> new RuntimeException("KYC request not found: " + kycRequestId));

        if (!kyc.getBranchId().equals(employee.getBranchId())) {
            throw new RuntimeException("Unauthorized: You cannot review KYC for another branch");
        }

        String decision = request.getDecision().trim().toUpperCase();
        kyc.setStatus(decision.equals("APPROVE") ? "APPROVED" : "REJECTED");
        kyc.setReviewedByUserId(currentUser.getId());
        kyc.setReviewComment(request.getComment());
        kyc.setReviewedAt(Instant.now());

        KycRequest saved = kycRequestRepository.save(kyc);

        Customer customer = customerRepository.findById(kyc.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer record missing"));

        customer.setKycStatus(kyc.getStatus());
        customerRepository.save(customer);

        log.info("KYC {} for Customer: {} by Employee: {}", kyc.getStatus(), customer.getCustomerNo(), currentUser.getUsername());
        return toResponse(saved);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    /**
     * Helper to map Entity to Response with human-readable codes
     */
    private KycResponse toResponse(KycRequest k) {
        // Fetch Customer to get customerNo
        Customer customer = customerRepository.findById(k.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Internal Error: Customer not found for KYC"));

        // Fetch Branch to get branchCode
        Branch branch = branchRepository.findById(k.getBranchId())
                .orElseThrow(() -> new RuntimeException("Internal Error: Branch not found for KYC"));

        return KycResponse.builder()
                .id(k.getId())
                .customerNo(customer.getCustomerNo()) // Mapped from Customer Entity
                .branchCode(branch.getBranchCode()) // Mapped from Branch Entity
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