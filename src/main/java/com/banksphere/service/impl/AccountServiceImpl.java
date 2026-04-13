package com.banksphere.service.impl;

import com.banksphere.dto.account.AccountResponse;
import com.banksphere.entity.Customer;
import com.banksphere.entity.User;
import com.banksphere.repository.AccountRepository;
import com.banksphere.repository.CustomerRepository;
import com.banksphere.repository.UserRepository;
import com.banksphere.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> myAccounts() {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Customer not found for userId=" + user.getId()));

        return accountRepository.findByCustomerIdOrderByOpenedAtDesc(customer.getId())
                .stream()
                .map(a -> AccountResponse.builder()
                        .id(a.getId())
                        .accountNo(a.getAccountNo())
                        .customerId(a.getCustomerId())
                        .branchId(a.getBranchId())
                        .accountType(a.getAccountType())
                        .status(a.getStatus().name())
                        .currency(a.getCurrency())
                        .availableBalance(a.getAvailableBalance())
                        .openedAt(a.getOpenedAt())
                        .build())
                .toList();
    }
}