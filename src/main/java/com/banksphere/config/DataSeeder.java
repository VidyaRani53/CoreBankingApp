package com.banksphere.config;

import com.banksphere.entity.Customer;
import com.banksphere.entity.Role;
import com.banksphere.entity.User;
import com.banksphere.repository.CustomerRepository;
import com.banksphere.repository.RoleRepository;
import com.banksphere.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        createRoleIfMissing("ROLE_ADMIN");
        createRoleIfMissing("ROLE_CSR");
        createRoleIfMissing("ROLE_CUSTOMER");
        createRoleIfMissing("ROLE_BRANCH_MANAGER");

        seedAdmin();
        seedDemoCustomer();
    }

    private void seedAdmin() {
        String adminUsername = "admin";
        if (!userRepository.existsByUsername(adminUsername)) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();

            User admin = User.builder()
                    .username(adminUsername)
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .userType("ADMIN")
                    .status("ACTIVE")
                    .failedLoginAttempts(0)
                    .build();

            admin.getRoles().add(adminRole);
            userRepository.save(admin);

            log.info("Seeded default admin user: username=admin password=Admin@123");
        }
    }

    private void seedDemoCustomer() {
        String username = "customer1";
        String rawPassword = "Customer@123";

        if (!userRepository.existsByUsername(username)) {
            Role customerRole = roleRepository.findByName("ROLE_CUSTOMER").orElseThrow();

            User user = User.builder()
                    .username(username)
                    .passwordHash(passwordEncoder.encode(rawPassword))
                    .userType("CUSTOMER")
                    .status("ACTIVE")
                    .failedLoginAttempts(0)
                    .build();

            user.getRoles().add(customerRole);
            User savedUser = userRepository.save(user);

            Customer customer = Customer.builder()
                    .userId(savedUser.getId())
                    .customerNo("CUST0001")
                    .fullName("Demo Customer")
                    .gender("OTHER")
                    .email("customer1@demo.com")
                    .phone("9999999999")
                    .addressLine1("Demo Address Line 1")
                    .city("Hyderabad")
                    .state("Telangana")
                    .pincode("500001")
                    .country("India")
                    .kycStatus("PENDING")
                    .build();

            customerRepository.save(customer);

            log.info("Seeded demo customer: username={} password={}", username, rawPassword);
        }
    }

    private void createRoleIfMissing(String roleName) {
        roleRepository.findByName(roleName).orElseGet(() -> {
            Role r = Role.builder().name(roleName).build();
            roleRepository.save(r);
            log.info("Seeded role: {}", roleName);
            return r;
        });
    }
}