package com.banksphere.service;

import com.banksphere.dto.account.AccountResponse;

import java.util.List;

public interface AdminAccountService {

    List<AccountResponse> getAccountsByBranch(
            String branchCode,
            String status,
            String accountType,
            String sortBy,
            String sortDir
    );
}
