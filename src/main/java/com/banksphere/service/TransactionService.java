package com.banksphere.service;

import com.banksphere.dto.txn.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TransactionService {
    TxnResponse deposit(DepositRequest request);
    TxnResponse withdraw(WithdrawRequest request);
    TxnResponse transfer(TransferRequest request);
    List<TxnResponse> getPendingHighValueTxns();

    TxnResponse approveHighValueTxn(UUID txnId);

    TxnResponse rejectHighValueTxn(UUID txnId, String reason);
    List<TxnResponse> getStatement(String accountNo, String interval, LocalDate from, LocalDate to, String type);

    List<TxnResponse> accountTransactions(UUID accountId, LocalDate from, LocalDate to, String type);
}