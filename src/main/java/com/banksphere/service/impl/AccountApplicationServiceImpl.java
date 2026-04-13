package com.banksphere.service.impl;

import com.banksphere.dto.account.*;
import com.banksphere.entity.*;
import com.banksphere.entity.enums.*;
import com.banksphere.repository.*;
import com.banksphere.service.AccountApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountApplicationServiceImpl implements AccountApplicationService {

    private final AccountApplicationRepository accountApplicationRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final BranchRepository branchRepository;

    @Override
    @Transactional
    public AccountApplicationResponse submit(AccountApplicationSubmitRequest request) {
        User user = getCurrentUser();

        if (!"CUSTOMER".equalsIgnoreCase(user.getUserType())) {
            throw new RuntimeException("Access Denied: Only CUSTOMER can submit account applications");
        }

        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Customer profile not found for user: " + user.getUsername()));

        if (!"APPROVED".equalsIgnoreCase(customer.getKycStatus())) {
            throw new RuntimeException("Compliance Error: KYC must be APPROVED before opening an account");
        }

        Branch branch = branchRepository.findByBranchCode(request.getPreferredBranchCode().trim())
                .orElseThrow(() -> new RuntimeException("Invalid Branch Code: " + request.getPreferredBranchCode()));

        BigDecimal initialDeposit = request.getInitialDeposit() == null ? BigDecimal.ZERO : request.getInitialDeposit();
        if (initialDeposit.signum() < 0) throw new RuntimeException("Validation Error: initialDeposit cannot be negative");

        AccountApplication app = AccountApplication.builder()
                .customerId(customer.getId())
                .preferredBranchId(branch.getId())
                .accountType(request.getAccountType())
                .initialDeposit(initialDeposit)
                .nomineeName(request.getNomineeName())
                .nomineeRelation(request.getNomineeRelation())
                .employmentType(request.getEmploymentType())
                .monthlyIncome(request.getMonthlyIncome())
                .purpose(request.getPurpose())
                .status(ApplicationStatus.SUBMITTED)
                .submittedAt(Instant.now())
                .build();

        AccountApplication saved = accountApplicationRepository.save(app);
        log.info("Application {} submitted by customer {}", saved.getId(), customer.getCustomerNo());

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountApplicationResponse> myApplications() {
        User user = getCurrentUser();
        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Customer profile not found"));

        return accountApplicationRepository.findByCustomerIdOrderBySubmittedAtDesc(customer.getId())
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountApplicationResponse> pendingForMyBranch() {
        User user = getCurrentUser();
        Employee employee = employeeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Employee profile missing"));

        return accountApplicationRepository
                .findByPreferredBranchIdAndStatusOrderBySubmittedAtDesc(employee.getBranchId(), ApplicationStatus.SUBMITTED)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public AccountResponse approve(UUID applicationId, ApplicationReviewRequest request) {
        User reviewer = getCurrentUser();
        Employee employee = employeeRepository.findByUserId(reviewer.getId())
                .orElseThrow(() -> new RuntimeException("Employee profile missing"));

        AccountApplication app = accountApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (app.getStatus() != ApplicationStatus.SUBMITTED) {
            throw new RuntimeException("State Error: Application is already processed");
        }

        if (!employee.getBranchId().equals(app.getPreferredBranchId())) {
            throw new RuntimeException("Security Error: Branch mismatch for approval");
        }

        Customer customer = customerRepository.findById(app.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer record missing"));

        // Mandatory check for compliance
        if (!"APPROVED".equalsIgnoreCase(customer.getKycStatus())) {
            throw new RuntimeException("Approval Blocked: Customer KYC is no longer approved");
        }

        String accountNo = generateAccountNo(app.getPreferredBranchId());

        Account account = Account.builder()
                .accountNo(accountNo)
                .customerId(customer.getId())
                .branchId(app.getPreferredBranchId())
                .accountType(app.getAccountType())
                .status(AccountStatus.ACTIVE)
                .currency("INR")
                .availableBalance(app.getInitialDeposit())
                .openedAt(Instant.now())
                .build();

        accountRepository.save(account);

        app.setStatus(ApplicationStatus.APPROVED);
        app.setReviewedByUserId(reviewer.getId());
        app.setReviewedAt(Instant.now());
        app.setRemarks(request.getComment());
        accountApplicationRepository.save(app);

        log.info("Account {} created for customer {}", accountNo, customer.getCustomerNo());

        return toAccountResponse(account);
    }

    @Override
    @Transactional
    public AccountApplicationResponse reject(UUID applicationId, ApplicationReviewRequest request) {
        User reviewer = getCurrentUser();
        Employee employee = employeeRepository.findByUserId(reviewer.getId())
                .orElseThrow(() -> new RuntimeException("Employee profile missing"));

        AccountApplication app = accountApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!employee.getBranchId().equals(app.getPreferredBranchId())) {
            throw new RuntimeException("Security Error: Branch mismatch");
        }

        app.setStatus(ApplicationStatus.REJECTED);
        app.setReviewedByUserId(reviewer.getId());
        app.setReviewedAt(Instant.now());
        app.setRemarks(request.getComment());

        return toResponse(accountApplicationRepository.save(app));
    }

    // ---------------- Finalized Helpers ----------------

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User session not found"));
    }

    private String generateAccountNo(UUID branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Internal Error: Branch context missing"));
        return String.format("%s-%d", branch.getBranchCode(), System.currentTimeMillis() % 100000000L);
    }

    /**
     * Maps Application Entity to Response with Branch Code and Customer No
     */
    private AccountApplicationResponse toResponse(AccountApplication a) {
        Customer customer = customerRepository.findById(a.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Internal mapping error: Customer not found"));

        Branch branch = branchRepository.findById(a.getPreferredBranchId())
                .orElseThrow(() -> new RuntimeException("Internal mapping error: Branch not found"));

        return AccountApplicationResponse.builder()
                .id(a.getId())
                .customerNo(customer.getCustomerNo())
                .preferredBranchCode(branch.getBranchCode())
                .accountType(a.getAccountType())
                .initialDeposit(a.getInitialDeposit())
                .status(a.getStatus().name())
                .submittedAt(a.getSubmittedAt())
                .reviewedByUserId(a.getReviewedByUserId())
                .reviewedAt(a.getReviewedAt())
                .remarks(a.getRemarks())
                .build();
    }

    /**
     * Maps Final Account Entity to Response with Branch Code and Customer No
     */
    private AccountResponse toAccountResponse(Account acc) {
        Customer customer = customerRepository.findById(acc.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Internal mapping error: Customer not found"));

        Branch branch = branchRepository.findById(acc.getBranchId())
                .orElseThrow(() -> new RuntimeException("Internal mapping error: Branch not found"));

        return AccountResponse.builder()
                .id(acc.getId())
                .accountNo(acc.getAccountNo())
                .customerNo(customer.getCustomerNo())
                .branchCode(branch.getBranchCode())
                .accountType(acc.getAccountType())
                .status(acc.getStatus().name())
                .currency(acc.getCurrency())
                .availableBalance(acc.getAvailableBalance())
                .openedAt(acc.getOpenedAt())
                .build();
    }
}