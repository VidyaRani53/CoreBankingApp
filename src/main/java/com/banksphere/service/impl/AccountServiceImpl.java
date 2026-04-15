package com.banksphere.service.impl;

import com.banksphere.dto.account.AccountApplicationSubmitRequest;
import com.banksphere.dto.account.AccountResponse;
import com.banksphere.entity.Account;
import com.banksphere.entity.Branch;
import com.banksphere.entity.Customer;
import com.banksphere.entity.User;
import com.banksphere.entity.enums.AccountStatus;
import com.banksphere.entity.enums.AccountType;
import com.banksphere.repository.AccountRepository;
import com.banksphere.repository.BranchRepository;
import com.banksphere.repository.CustomerRepository;
import com.banksphere.repository.UserRepository;
import com.banksphere.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.sort;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final BranchRepository branchRepository;

    @Override
    @Transactional
    public AccountResponse submitApplication(AccountApplicationSubmitRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Logged-in user not found: " + username));

        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Customer profile missing for user: " + username));

        Branch branch = branchRepository.findByBranchCode(request.getPreferredBranchCode().trim())
                .orElseThrow(() -> new RuntimeException("Invalid Branch Code provided: " + request.getPreferredBranchCode()));

        String generatedAccountNo = String.format("ACC-%s-%d",
                branch.getBranchCode(),
                System.currentTimeMillis() % 1000000);

        Account account = Account.builder()
                .accountNo(generatedAccountNo)
                .customerId(customer.getId())
                .branchId(branch.getId())
                .branchCode(branch.getBranchCode())
                .accountType(request.getAccountType())
                .status(AccountStatus.ACTIVE)
                .currency("INR")
                .availableBalance(request.getInitialDeposit())
                .openedAt(Instant.now())
                .build();

        Account saved = accountRepository.save(account);

        log.info("New account created: {} for customer: {}", saved.getAccountNo(), customer.getCustomerNo());

        // Pass the already fetched customer and branch to the mapper
        return mapToResponse(saved, customer, branch);
    }

    @Override
    public AccountResponse getAccountById(UUID accountId) {

        User user = getCurrentUser();

        Account account;

        switch (user.getUserType()) {

            case "ADMIN" -> {
                account = accountRepository.findById(accountId)
                        .orElseThrow(() -> new RuntimeException("Account not found"));
            }

            case "ROLE_BRANCH_MANAGER" -> {
                account = accountRepository
                        .findByIdAndBranchCode(accountId, user.getBranchCode())
                        .orElseThrow(() -> new RuntimeException(
                                "Access denied or account not found in your branch"));
            }

            case "ROLE_CSR" -> {
                account = accountRepository.findById(accountId)
                        .orElseThrow(() -> new RuntimeException("Account not found"));
            }

            case "CUSTOMER" -> {
                Customer customer = customerRepository.findByUserId(user.getId())
                        .orElseThrow(() -> new RuntimeException("Customer profile not found"));

                account = accountRepository
                        .findByIdAndCustomerId(accountId, customer.getId())
                        .orElseThrow(() -> new RuntimeException(
                                "Access denied: This account does not belong to you"));
            }

            default -> throw new RuntimeException("Invalid user role");
        }

        return toResponse(account);
    }

    @Override
    public List<AccountResponse> getAccountsByCustomerId(UUID customerId) {

        User user = getCurrentUser();

        List<Account> accounts;

        switch (user.getUserType()) {

            case "ADMIN", "ROLE_CSR" -> {
                // ✅ Full access
                accounts = accountRepository.findByCustomerId(customerId);
            }

            case "ROLE_BRANCH_MANAGER" -> {
                // ✅ Only own branch customers
                String branchCode = user.getBranchCode();
                accounts = accountRepository
                        .findByCustomerIdAndBranchCode(customerId, branchCode);
            }

            case "CUSTOMER" -> {
                // ✅ Only own customerId
                Customer customer = customerRepository.findByUserId(user.getId())
                        .orElseThrow(() -> new RuntimeException("Customer profile not found"));

                if (!customer.getId().equals(customerId)) {
                    throw new RuntimeException("Access denied: Cannot view other customers' accounts");
                }

                accounts = accountRepository.findByCustomerId(customerId);
            }

            default -> throw new RuntimeException("Invalid user role");
        }

        return accounts.stream()
                .map(this::toResponse)
                .toList();
    }






    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> myAccounts() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Customer profile not found"));

        return accountRepository.findByCustomerIdOrderByOpenedAtDesc(customer.getId())
                .stream()
                .map(account -> {
                    // Fetch the specific branch for each account in the list
                    Branch branch = branchRepository.findById(account.getBranchId())
                            .orElseThrow(() -> new RuntimeException("Branch not found for account"));
                    return mapToResponse(account, customer, branch);
                })
                .toList();
    }
    @Override
    public AccountResponse closeAccount(String accountNo) {

        User user = getCurrentUser();

        Account account = accountRepository
                .findByAccountNoAndStatusNot(accountNo, AccountStatus.CLOSED)
                .orElseThrow(() -> new RuntimeException("Account not found or already closed"));

        // ✅ Only ADMIN or BRANCH_MANAGER
        if (!isAdminOrManager(user)) {
            throw new RuntimeException("Access denied");
        }

        account.setStatus(AccountStatus.CLOSED);
        account.setClosedAt(Instant.now());
        account.setClosedBy(user.getId());

        return toResponse(accountRepository.save(account));
    }

    @Override
    public AccountResponse freezeAccount(String accountNo) {

        Account account = accountRepository.findByAccountNo(accountNo)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("Only ACTIVE accounts can be frozen");
        }

        account.setStatus(AccountStatus.FROZEN);
        return toResponse(accountRepository.save(account));
    }

    @Override
    public AccountResponse unfreezeAccount(String accountNo) {

        Account account = accountRepository.findByAccountNo(accountNo)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (account.getStatus() != AccountStatus.FROZEN) {
            throw new RuntimeException("Account is not frozen");
        }

        account.setStatus(AccountStatus.ACTIVE);
        return toResponse(accountRepository.save(account));
    }
    @Override
    public List<AccountResponse> getMyBranchAccounts(
            String status,
            String accountType,
            String sortBy,
            String sortDir) {

        User user = getCurrentUser();

        // ✅ Only Branch Manager
        if (!"ROLE_BRANCH_MANAGER".equalsIgnoreCase(user.getUserType())) {
            throw new RuntimeException("Access denied: Only Branch Manager allowed");
        }

        String branchCode = user.getBranchCode();

        List<Account> accounts = fetchAccounts(
                branchCode, status, accountType
        );

        sort(accounts, sortBy, sortDir);

        return accounts.stream()
                .map(this::toResponse)
                .toList();
    }


    // ---------------- HELPERS ----------------

    private boolean isAdminOrManager(User user) {
        return "ADMIN".equalsIgnoreCase(user.getUserType())
                || "ROLE_BRANCH_MANAGER".equalsIgnoreCase(user.getUserType());
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByUsername(username).orElseThrow();
    }

    private AccountResponse toResponse(Account a) {

        String customerNo = customerRepository.findById(a.getCustomerId())
                .map(Customer::getCustomerNo)
                .orElse("UNKNOWN");

        return AccountResponse.builder()
                .id(a.getId())
                .accountNo(a.getAccountNo())
                .customerNo(customerNo)
                .branchCode(a.getBranchCode())
                .accountType(a.getAccountType())
                .status(a.getStatus().name())
                .currency(a.getCurrency())
                .availableBalance(a.getAvailableBalance())
                .openedAt(a.getOpenedAt())
                .build();
    }
    private List<Account> fetchAccounts(
            String branchCode,
            String status,
            String accountType) {

        // ✅ BOTH filters
        if (status != null && accountType != null) {
            return accountRepository.findByBranchCodeAndStatusAndAccountType(
                    branchCode,
                    AccountStatus.valueOf(status.toUpperCase()),
                    AccountType.valueOf(accountType.toUpperCase())
            );
        }

        // ✅ STATUS only
        if (status != null) {
            return accountRepository.findByBranchCodeAndStatus(
                    branchCode,
                    AccountStatus.valueOf(status.toUpperCase())
            );
        }

        // ✅ ACCOUNT TYPE only  ✅✅✅ THIS WAS MISSING
        if (accountType != null) {
            return accountRepository.findByBranchCodeAndAccountType(
                    branchCode,
                    AccountType.valueOf(accountType.toUpperCase())
            );
        }

        // ✅ NO filters
        return accountRepository.findByBranchCode(branchCode);
    }

    private void sort(
            List<Account> accounts,
            String sortBy,
            String sortDir) {

        if (sortBy == null) return;

        Comparator<Account> comparator = switch (sortBy) {
            case "accountType" -> Comparator.comparing(Account::getAccountType);
            case "openedAt" -> Comparator.comparing(Account::getOpenedAt);
            default -> null;
        };

        if (comparator != null && "desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }

        if (comparator != null) {
            accounts.sort(comparator);
        }
    }


    /**
     * Modified mapper to accept Customer and Branch entities
     * to populate human-readable Codes/Numbers.
     */
    private AccountResponse mapToResponse(Account a, Customer c, Branch b) {
        return AccountResponse.builder()
                .id(a.getId())
                .accountNo(a.getAccountNo())
                .customerNo(c.getCustomerNo())
                .branchCode(b.getBranchCode())
                .accountType(a.getAccountType())
                .status(a.getStatus().name())
                .currency(a.getCurrency())
                .availableBalance(a.getAvailableBalance())
                .openedAt(a.getOpenedAt())
                .build();
    }






}