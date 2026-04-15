package com.banksphere.service.impl;

import com.banksphere.dto.account.AccountResponse;
import com.banksphere.entity.Account;
import com.banksphere.entity.Customer;
import com.banksphere.entity.User;
import com.banksphere.entity.enums.AccountStatus;
import com.banksphere.entity.enums.AccountType;
import com.banksphere.repository.AccountRepository;
import com.banksphere.repository.CustomerRepository;
import com.banksphere.repository.UserRepository;
import com.banksphere.service.AdminAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAccountServiceImpl implements AdminAccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    @Override
    public List<AccountResponse> getAccountsByBranch(
            String branchCode,
            String status,
            String accountType,
            String sortBy,
            String sortDir) {

        User user = getCurrentUser();

        String effectiveBranchCode = resolveBranchCode(user, branchCode);

        List<Account> accounts = fetchAccounts(
                effectiveBranchCode, status, accountType
        );

        sort(accounts, sortBy, sortDir);

        return accounts.stream()
                .map(this::toResponse)
                .toList();
    }

    // ---------------- HELPERS ----------------

    private String resolveBranchCode(User user, String inputBranchCode) {

        if ("ADMIN".equalsIgnoreCase(user.getUserType())) {
            if (inputBranchCode == null)
                throw new RuntimeException("branchCode is required for ADMIN");
            return inputBranchCode;
        }

        if ("BRANCH_MANAGER".equalsIgnoreCase(user.getUserType())) {
            return user.getBranchCode();
        }

        throw new RuntimeException("Access denied");
    }

    /**
     * ✅ FIXED LOGIC – handles ALL filter combinations
     */
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

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByUsername(username).orElseThrow();
    }
}