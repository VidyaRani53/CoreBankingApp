package com.banksphere.service;

import com.banksphere.dto.account.AccountResponse;

import java.util.List;

public interface AccountService {
    List<AccountResponse> myAccounts();
}