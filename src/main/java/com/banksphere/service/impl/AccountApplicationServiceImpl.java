package com.banksphere.service.impl;

import com.banksphere.dto.account.*;
import com.banksphere.entity.*;
import com.banksphere.entity.enums.*;
import com.banksphere.repository.*;
import com.banksphere.service.AccountApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    @Transactional
    public AccountApplicationResponse submit(AccountApplicationSubmitRequest request) {
        User user = getCurrentUser();

        if (!"CUSTOMER".equalsIgnoreCase(user.getUserType())) {
            throw new RuntimeException("Only CUSTOMER can submit account application");
        }

        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Customer not found for userId=" + user.getId()));

        // optional rule (recommended): require KYC APPROVED before applying
        // If you want to allow apply before KYC, comment this block.
        if (!"APPROVED".equalsIgnoreCase(customer.getKycStatus())) {
            throw new RuntimeException("KYC must be APPROVED to submit account application");
        }

        BigDecimal initialDeposit = request.getInitialDeposit() == null ? BigDecimal.ZERO : request.getInitialDeposit();
        if (initialDeposit.signum() < 0) throw new RuntimeException("initialDeposit must be >= 0");

        AccountApplication app = AccountApplication.builder()
                .customerId(customer.getId())
                .preferredBranchId(request.getPreferredBranchId())
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

        log.info("Account application submitted: appId={}, customerId={}, preferredBranchId={}, type={}",
                saved.getId(), saved.getCustomerId(), saved.getPreferredBranchId(), saved.getAccountType());

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountApplicationResponse> myApplications() {
        User user = getCurrentUser();

        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Customer not found for userId=" + user.getId()));

        return accountApplicationRepository.findByCustomerIdOrderBySubmittedAtDesc(customer.getId())
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountApplicationResponse> pendingForMyBranch() {
        User user = getCurrentUser();

        Employee employee = employeeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Employee not found for userId=" + user.getId()));

        UUID branchId = employee.getBranchId();

        return accountApplicationRepository
                .findByPreferredBranchIdAndStatusOrderBySubmittedAtDesc(branchId, ApplicationStatus.SUBMITTED)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public AccountResponse approve(UUID applicationId, ApplicationReviewRequest request) {
        User user = getCurrentUser();

        if (!"EMPLOYEE".equalsIgnoreCase(user.getUserType()) && !"ADMIN".equalsIgnoreCase(user.getUserType())) {
            throw new RuntimeException("Only EMPLOYEE/ADMIN can approve account application");
        }

        Employee employee = employeeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Employee not found for userId=" + user.getId()));

        AccountApplication app = accountApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found: " + applicationId));

        if (app.getStatus() != ApplicationStatus.SUBMITTED) {
            throw new RuntimeException("Application is not in SUBMITTED state");
        }

        // Branch authorization: CSR can approve only for their branch
        if (!employee.getBranchId().equals(app.getPreferredBranchId())) {
            throw new RuntimeException("You cannot approve applications for other branches");
        }

        Customer customer = customerRepository.findById(app.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found: " + app.getCustomerId()));

        // Mandatory rule you asked in spec:
        if (!"APPROVED".equalsIgnoreCase(customer.getKycStatus())) {
            throw new RuntimeException("Customer KYC must be APPROVED before account creation");
        }

        String accountNo = generateAccountNo(app.getPreferredBranchId());

        Account account = Account.builder()
                .accountNo(accountNo)
                .customerId(customer.getId())
                .branchId(app.getPreferredBranchId())
                .accountType(app.getAccountType())
                .status(AccountStatus.ACTIVE)
                .currency("INR")
                .availableBalance(app.getInitialDeposit() == null ? BigDecimal.ZERO : app.getInitialDeposit())
                .openedAt(Instant.now())
                .build();

        Account savedAccount = accountRepository.save(account);

        app.setStatus(ApplicationStatus.APPROVED);
        app.setReviewedByUserId(user.getId());
        app.setReviewedAt(Instant.now());
        app.setRemarks(request.getComment());
        accountApplicationRepository.save(app);

        log.info("Account application approved: appId={}, accountId={}, accountNo={}, customerId={}, branchId={}",
                app.getId(), savedAccount.getId(), savedAccount.getAccountNo(), customer.getId(), savedAccount.getBranchId());

        return toAccountResponse(savedAccount);
    }

    @Override
    @Transactional
    public AccountApplicationResponse reject(UUID applicationId, ApplicationReviewRequest request) {
        User user = getCurrentUser();

        Employee employee = employeeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Employee not found for userId=" + user.getId()));

        AccountApplication app = accountApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found: " + applicationId));

        if (app.getStatus() != ApplicationStatus.SUBMITTED) {
            throw new RuntimeException("Application is not in SUBMITTED state");
        }

        if (!employee.getBranchId().equals(app.getPreferredBranchId())) {
            throw new RuntimeException("You cannot reject applications for other branches");
        }

        app.setStatus(ApplicationStatus.REJECTED);
        app.setReviewedByUserId(user.getId());
        app.setReviewedAt(Instant.now());
        app.setRemarks(request.getComment());
        AccountApplication saved = accountApplicationRepository.save(app);

        log.info("Account application rejected: appId={}, byUserId={}", saved.getId(), user.getId());

        return toResponse(saved);
    }

    // ---------------- helpers ----------------

    private User getCurrentUser() {
        // Use your existing pattern. If you have a SecurityUtil, replace this accordingly.
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    private String generateAccountNo(UUID branchId) {
        // Simple unique demo-friendly generation; later we can add branchCode prefix.
        return "AC-" + System.currentTimeMillis();
    }

    private AccountApplicationResponse toResponse(AccountApplication a) {
        return AccountApplicationResponse.builder()
                .id(a.getId())
                .customerId(a.getCustomerId())
                .preferredBranchId(a.getPreferredBranchId())
                .accountType(a.getAccountType())
                .initialDeposit(a.getInitialDeposit())
                .status(a.getStatus().name())
                .submittedAt(a.getSubmittedAt())
                .reviewedByUserId(a.getReviewedByUserId())
                .reviewedAt(a.getReviewedAt())
                .remarks(a.getRemarks())
                .build();
    }

    private AccountResponse toAccountResponse(Account acc) {
        return AccountResponse.builder()
                .id(acc.getId())
                .accountNo(acc.getAccountNo())
                .customerId(acc.getCustomerId())
                .branchId(acc.getBranchId())
                .accountType(acc.getAccountType())
                .status(acc.getStatus().name())
                .currency(acc.getCurrency())
                .availableBalance(acc.getAvailableBalance())
                .openedAt(acc.getOpenedAt())
                .build();
    }
}