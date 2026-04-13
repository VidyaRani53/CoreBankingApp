package com.banksphere.repository;

import com.banksphere.entity.Transaction;
import com.banksphere.entity.enums.TxnType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByTxnRef(String txnRef);

    List<Transaction> findByFromAccountIdOrToAccountIdOrderByRequestedAtDesc(UUID from, UUID to);

    List<Transaction> findByRequestedAtBetweenAndTxnType(Instant from, Instant to, TxnType type);
    // add in TransactionRepository
    List<Transaction> findByFromAccountIdOrToAccountIdAndRequestedAtBetweenOrderByRequestedAtDesc(
            UUID fromAccountId, UUID toAccountId, Instant from, Instant to);
}