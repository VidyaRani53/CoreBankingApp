package com.banksphere.service;

import com.banksphere.dto.account.AccountApplicationSubmitRequest;
import com.banksphere.dto.account.AccountResponse;

import java.util.List;

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
}