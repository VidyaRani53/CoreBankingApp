package com.banksphere.service.impl;

import com.banksphere.dto.account.AccountApplicationSubmitRequest;
import com.banksphere.dto.account.AccountResponse;
import com.banksphere.entity.Account;
import com.banksphere.entity.Branch;
import com.banksphere.entity.Customer;
import com.banksphere.entity.User;
import com.banksphere.entity.enums.AccountStatus;
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
import java.util.List;
import java.util.UUID;

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