package com.banksphere.service.impl;

import com.banksphere.dto.LoginRequest;
import com.banksphere.dto.LoginResponse;
import com.banksphere.dto.RegisterRequest;
import com.banksphere.entity.Role;
import com.banksphere.entity.User;
import com.banksphere.repository.RoleRepository;
import com.banksphere.repository.UserRepository;
import com.banksphere.security.jwt.JwtUtil;
import com.banksphere.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.banksphere.entity.Customer;
import com.banksphere.repository.CustomerRepository;
import org.springframework.transaction.annotation.Transactional;
import com.banksphere.repository.BranchRepository;
import java.util.UUID;
import com.banksphere.entity.Branch;
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CustomerRepository customerRepository;
    private final BranchRepository branchRepository;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        String username = request.getUsername() == null ? "" : request.getUsername().trim();

        if (username.isEmpty()) {
            throw new RuntimeException("Username is required");
        }

        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new RuntimeException("ROLE_CUSTOMER not found. Seed roles first."));

        User user = User.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .userType("CUSTOMER")
                .status("ACTIVE")
                .failedLoginAttempts(0)
                .build();

        user.getRoles().add(customerRole);

        User savedUser = userRepository.save(user);

        String branchCode = request.getBranchCode() == null ? "" : request.getBranchCode().trim();
        if (branchCode.isEmpty()) {
            throw new RuntimeException("branchCode is required");
        }

        Branch branch = branchRepository.findByBranchCode(branchCode)
                .orElseThrow(() -> new RuntimeException("Invalid branchCode: " + branchCode));

        Customer customer = Customer.builder()
                .userId(savedUser.getId())
                .customerNo(generateCustomerNo())
                .branchId(branch.getId())
                .fullName(request.getFullName())
                .dob(request.getDob())
                .gender(request.getGender())
                .email(request.getEmail())
                .phone(request.getPhone())
                .country("India")
                .kycStatus("NOT_SUBMITTED")
                .build();

        customerRepository.save(customer);

        log.info("Registered new customer user: {}, customerNo={}", username, customer.getCustomerNo());
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername());
        return new LoginResponse(token, "Bearer");
    }

    private String generateCustomerNo() {
        return "CUST-" + System.currentTimeMillis();
    }
}