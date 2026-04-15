package com.banksphere.service.impl;

import com.banksphere.entity.Customer;
import com.banksphere.entity.CustomerUpdateRequest;
import com.banksphere.repository.CustomerRepository;
import com.banksphere.repository.CustomerUpdateRequestRepository;
import com.banksphere.repository.UserRepository;
import com.banksphere.service.CSRService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
@Service
@RequiredArgsConstructor
public class CSRServiceImpl implements CSRService {
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final CustomerUpdateRequestRepository updateRequestRepository;


    @Transactional
    public void approveUpdate(Long requestId) {

        CustomerUpdateRequest request = updateRequestRepository.findById(requestId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Update request not found: " + requestId));

        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalStateException("Request already processed");
        }

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Customer not found: " + request.getCustomerId()));

        // ✅ Apply approved fields
        customer.setFullName(request.getFullName());
        customer.setDob(request.getDob());
        customer.setGender(request.getGender());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddressLine1(request.getAddressLine1());
        customer.setCity(request.getCity());
        customer.setState(request.getState());
        customer.setPincode(request.getPincode());

        // ✅ Mark request as approved
        request.setStatus("APPROVED");
        request.setProcessedAt(Instant.now());
    }

    @Transactional
    public void rejectUpdate(Long requestId, String reason) {

        CustomerUpdateRequest request = updateRequestRepository.findById(requestId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Update request not found: " + requestId));

        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalStateException("Request already processed");
        }

        request.setStatus("REJECTED");
        request.setRejectionReason(reason);
        request.setProcessedAt(Instant.now());
    }

    public List<CustomerUpdateRequest> getPendingRequests() {
        return updateRequestRepository.findByStatus("PENDING");
    }
}
