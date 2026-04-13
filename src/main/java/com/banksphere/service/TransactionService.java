package com.banksphere.service;

import com.banksphere.dto.txn.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TransactionService {
    TxnResponse deposit(DepositRequest request);
    TxnResponse withdraw(WithdrawRequest request);
    TxnResponse transfer(TransferRequest request);

    List<TxnResponse> accountTransactions(UUID accountId, LocalDate from, LocalDate to, String type);
}