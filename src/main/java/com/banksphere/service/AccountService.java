package com.banksphere.service;

import com.banksphere.dto.account.AccountApplicationSubmitRequest;
import com.banksphere.dto.account.AccountResponse;

import java.util.List;
import java.util.UUID;

public interface AccountService {

    /**
     * Submits a new account application for the logged-in customer.
     * Maps the branchCode to the internal branchId automatically.
     */
    AccountResponse submitApplication(AccountApplicationSubmitRequest request);

    /**
     * Retrieves all accounts belonging to the currently authenticated customer.
     */
    List<AccountResponse> myAccounts();
//    public List<AccountResponse> getAccountsForMyBranch();
    AccountResponse getAccountById(UUID accountId);
    List<AccountResponse> getAccountsByCustomerId(UUID customerId);
    AccountResponse closeAccount(String accountNo);

    AccountResponse freezeAccount(String accountNo);

    AccountResponse unfreezeAccount(String accountNo);

    List<AccountResponse> getMyBranchAccounts(
            String status,
            String accountType,
            String sortBy,
            String sortDir
    );


}