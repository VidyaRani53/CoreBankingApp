package com.banksphere.service.impl;

import com.banksphere.dto.employee.CreateEmployeeRequest;
import com.banksphere.dto.employee.CreateEmployeeResponse;
import com.banksphere.dto.employee.ResetPasswordResponse;
import com.banksphere.entity.Employee;
import com.banksphere.entity.Role;
import com.banksphere.entity.User;
import com.banksphere.repository.BranchRepository;
import com.banksphere.repository.EmployeeRepository;
import com.banksphere.repository.RoleRepository;
import com.banksphere.repository.UserRepository;
import com.banksphere.service.AdminEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.banksphere.dto.employee.EmployeeResponse;
import java.util.*;
import java.security.SecureRandom;

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
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        // ensure branch exists
        branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found: " + request.getBranchId()));

        String designation = request.getDesignation().trim().toUpperCase();
        String roleName = switch (designation) {
            case "CSR" -> "ROLE_CSR";
            case "BRANCH_MANAGER" -> "ROLE_BRANCH_MANAGER";
            default -> throw new RuntimeException("Invalid designation. Use CSR or BRANCH_MANAGER");
        };

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException(roleName + " not found. Seed roles first."));

        String tempPassword = generateTempPassword(12);

        User user = User.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(tempPassword))
                .userType("EMPLOYEE")
                .status("ACTIVE")
                .failedLoginAttempts(0)
                .build();
        user.getRoles().add(role);

        User savedUser = userRepository.save(user);

        Employee employee = Employee.builder()
                .userId(savedUser.getId())
                .branchId(request.getBranchId())
                .employeeNo(request.getEmployeeNo().trim())
                .designation(designation)
                .status("ACTIVE")
                .build();

        Employee savedEmp = employeeRepository.save(employee);

        return CreateEmployeeResponse.builder()
                .userId(savedUser.getId())
                .employeeId(savedEmp.getId())
                .username(savedUser.getUsername())
                .tempPassword(tempPassword)
                .roleAssigned(roleName)
                .branchId(savedEmp.getBranchId())
                .build();
    }

    private String generateTempPassword(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(ALPHANUM.charAt(RANDOM.nextInt(ALPHANUM.length())));
        }
        return sb.toString();
    }

    @Override
    public List<EmployeeResponse> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();

        // collect userIds
        Set<UUID> userIds = new HashSet<>();
        for (Employee e : employees) userIds.add(e.getUserId());

        // load users and map
        Map<UUID, User> userMap = new HashMap<>();
        for (User u : userRepository.findAllById(userIds)) {
            userMap.put(u.getId(), u);
        }

        List<EmployeeResponse> out = new ArrayList<>();
        for (Employee e : employees) {
            User u = userMap.get(e.getUserId());
            out.add(EmployeeResponse.builder()
                    .employeeId(e.getId())
                    .userId(e.getUserId())
                    .username(u != null ? u.getUsername() : null)
                    .employeeNo(e.getEmployeeNo())
                    .designation(e.getDesignation())
                    .status(e.getStatus())
                    .branchId(e.getBranchId())
                    .build());
        }
        return out;
    }

    @Override
    @Transactional
    public ResetPasswordResponse resetPassword(String username) {
        String u = username.trim();

        User user = userRepository.findByUsername(u)
                .orElseThrow(() -> new RuntimeException("User not found: " + u));

        // allow reset only for employees (optional but recommended)
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
}