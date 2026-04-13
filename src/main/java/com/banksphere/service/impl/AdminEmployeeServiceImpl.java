package com.banksphere.service.impl;

import com.banksphere.dto.employee.CreateEmployeeRequest;
import com.banksphere.dto.employee.CreateEmployeeResponse;
import com.banksphere.dto.employee.EmployeeResponse;
import com.banksphere.dto.employee.ResetPasswordResponse;
import com.banksphere.entity.*;
import com.banksphere.repository.*;
import com.banksphere.service.AdminEmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminEmployeeServiceImpl implements AdminEmployeeService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALPHANUM = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789@#";

    @Override
    @Transactional
    public CreateEmployeeResponse createEmployee(CreateEmployeeRequest request) {
        String username = request.getUsername().trim();

        // 1. Validate Username
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists: " + username);
        }

        // 2. Find Branch by Code
        Branch branch = branchRepository.findByBranchCode(request.getBranchCode().trim())
                .orElseThrow(() -> new RuntimeException("Branch not found with code: " + request.getBranchCode()));

        // 3. Generate Sequential Employee Number (e.g., BR0001-001)
        long currentEmpCount = employeeRepository.countByBranchId(branch.getId());
        String generatedEmpNo = String.format("%s-%03d", branch.getBranchCode(), currentEmpCount + 1);

        // 4. Map Designation to Security Role
        String designation = request.getDesignation().trim().toUpperCase();
        String roleName = switch (designation) {
            case "CSR" -> "ROLE_CSR";
            case "BRANCH_MANAGER" -> "ROLE_BRANCH_MANAGER";
            default -> throw new RuntimeException("Invalid designation. Use CSR or BRANCH_MANAGER");
        };

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException(roleName + " not found. Please seed roles in DB first."));

        // 5. Create User Entity
        String tempPassword = generateTempPassword(12);
        User user = User.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(tempPassword))
                .userType("EMPLOYEE")
                .status("ACTIVE")
                .failedLoginAttempts(0)
                .roles(new HashSet<>(Collections.singletonList(role)))
                .build();

        User savedUser = userRepository.save(user);

        // 6. Create Employee Entity
        Employee employee = Employee.builder()
                .userId(savedUser.getId())
                .branchId(branch.getId())
                .employeeNo(generatedEmpNo) // This is the professional ID
                .designation(designation)
                .status("ACTIVE")
                .build();

        Employee savedEmp = employeeRepository.save(employee);
        log.info("Employee created successfully: {} with Business No: {}", username, generatedEmpNo);

        // RETURN STATEMENT UPDATED HERE
        return CreateEmployeeResponse.builder()
                .userId(savedUser.getId())
                .employeeId(savedEmp.getId())
                .employeeNo(savedEmp.getEmployeeNo()) // Map the Business ID
                .username(savedUser.getUsername())
                .tempPassword(tempPassword)
                .roleAssigned(roleName)
                .branchId(savedEmp.getBranchId())
                .build();
    }

    @Override
    public List<EmployeeResponse> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        Set<UUID> userIds = new HashSet<>();
        for (Employee e : employees) userIds.add(e.getUserId());

        Map<UUID, User> userMap = new HashMap<>();
        userRepository.findAllById(userIds).forEach(u -> userMap.put(u.getId(), u));

        return employees.stream().map(e -> {
            User u = userMap.get(e.getUserId());
            return EmployeeResponse.builder()
                    .employeeId(e.getId())
                    .userId(e.getUserId())
                    .username(u != null ? u.getUsername() : "UNKNOWN")
                    .employeeNo(e.getEmployeeNo())
                    .designation(e.getDesignation())
                    .status(e.getStatus())
                    .branchId(e.getBranchId())
                    .build();
        }).toList();
    }

    @Override
    @Transactional
    public ResetPasswordResponse resetPassword(String username) {
        User user = userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        if (!"EMPLOYEE".equalsIgnoreCase(user.getUserType())) {
            throw new RuntimeException("Password reset allowed only for EMPLOYEE users");
        }

        String tempPassword = generateTempPassword(12);
        user.setPasswordHash(passwordEncoder.encode(tempPassword));
        userRepository.save(user);

        return ResetPasswordResponse.builder()
                .username(user.getUsername())
                .tempPassword(tempPassword)
                .build();
    }

    private String generateTempPassword(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(ALPHANUM.charAt(RANDOM.nextInt(ALPHANUM.length())));
        }
        return sb.toString();
    }
}