package com.banksphere.service.impl;

import com.banksphere.dto.customer.CustomerProfileUpdateRequest;
import com.banksphere.dto.customer.CustomerResponse;
import com.banksphere.entity.Customer;
import com.banksphere.entity.CustomerUpdateRequest;
import com.banksphere.entity.Employee;
import com.banksphere.entity.User;
import com.banksphere.repository.CustomerRepository;
import com.banksphere.repository.CustomerUpdateRequestRepository;
import com.banksphere.repository.EmployeeRepository;
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
    private final EmployeeRepository employeeRepository;
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

    @Override
    public CustomerResponse deactivateCustomer(String customerNo) {

        User actor = getCurrentUser();

        if (!isCsrOrManager(actor)) {
            throw new RuntimeException("Access denied: Only CSR or Branch Manager allowed");
        }

        Customer customer = customerRepository
                .findByCustomerNoAndStatus(customerNo, "ACTIVE")
                .orElseThrow(() -> new RuntimeException("Customer not found or already inactive"));

        // ✅ Branch restriction for Branch Manager
        if ("BRANCH_MANAGER".equalsIgnoreCase(actor.getUserType())
                && !customer.getBranchId().equals(getUserBranchId(actor))) {
            throw new RuntimeException("Access denied: Customer not in your branch");
        }

        customer.setStatus("INACTIVE");
        customer.setDeactivatedAt(Instant.now());
        customer.setDeactivatedBy(actor.getId());

        return toResponse(customerRepository.save(customer));
    }

    @Override
    public List<CustomerResponse> getCustomersInMyBranch(String status) {

        User user = getCurrentUser();

        // ✅ Only CSR or Branch Manager
        if (!isCsrOrManager(user)) {
            throw new RuntimeException("Access denied: Only CSR or Branch Manager allowed");
        }

        // ✅ Resolve branchId from Employee
        Employee employee = employeeRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException("Employee profile not found for user"));

        UUID branchId = employee.getBranchId();

        List<Customer> customers;

        if (status != null) {
            customers = customerRepository.findByBranchIdAndStatus(branchId, status.toUpperCase());
        } else {
            customers = customerRepository.findByBranchId(branchId);
        }

        return customers.stream()
                .map(this::toResponse)
                .toList();
    }




    // ---------------- HELPERS ----------------

    private boolean isCsrOrManager(User user) {
        return "ROLE_CSR".equalsIgnoreCase(user.getUserType())
                || "ROLE_BRANCH_MANAGER".equalsIgnoreCase(user.getUserType());
    }

    private UUID getUserBranchId(User user) {

        Employee employee = employeeRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Employee profile not found for user: " + user.getUsername()
                        ));

        return employee.getBranchId();
    }


    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByUsername(username).orElseThrow();
    }

    @Override
    @Transactional
    public CustomerResponse reactivateCustomer(String customerNo) {

        User actor = getCurrentUser();

        // ✅ Only CSR or Branch Manager
        if (!isCsrOrManager(actor)) {
            throw new RuntimeException("Access denied: Only CSR or Branch Manager allowed");
        }

        Customer customer = customerRepository
                .findByCustomerNoAndStatus(customerNo, "INACTIVE")
                .orElseThrow(() ->
                        new RuntimeException("Customer not found or already active"));

        // ✅ Branch Manager can restore only their branch customers
        if ("ROLE_BRANCH_MANAGER".equalsIgnoreCase(actor.getUserType())
                && !customer.getBranchId().equals(getUserBranchId(actor))) {
            throw new RuntimeException("Access denied: Customer not in your branch");
        }

        customer.setStatus("ACTIVE");
        customer.setDeactivatedAt(null);
        customer.setDeactivatedBy(null);

        return toResponse(customerRepository.save(customer));
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
                .status(c.getStatus())
                .kycStatus(c.getKycStatus())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }


}