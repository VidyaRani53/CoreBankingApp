package com.banksphere.repository;

import com.banksphere.entity.Transaction;
import com.banksphere.entity.enums.TxnType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    @Query("SELECT t FROM Transaction t WHERE (t.fromAccountId = :accId OR t.toAccountId = :accId) " +
            "AND t.requestedAt BETWEEN :start AND :end ORDER BY t.requestedAt DESC")
    List<Transaction> findStatement(@Param("accId") UUID accId,
                                    @Param("start") Instant start,
                                    @Param("end") Instant end);
}