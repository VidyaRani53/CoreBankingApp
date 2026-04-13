package com.banksphere.service.impl;

import com.banksphere.dto.txn.*;
import com.banksphere.entity.*;
import com.banksphere.entity.enums.*;
import com.banksphere.repository.*;
import com.banksphere.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final AccountRepository accountRepository;

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public TxnResponse deposit(DepositRequest request) {
        Transaction existing = transactionRepository.findByTxnRef(request.getTxnRef()).orElse(null);
        if (existing != null) return toResponse(existing);

        User user = getCurrentUser();
        Account account = loadOwnedAccount(user, request.getAccountId());

        validateAccountActive(account);
        BigDecimal amount = validateAmount(request.getAmount());

        Transaction txn = transactionRepository.save(Transaction.builder()
                .txnRef(request.getTxnRef())
                .txnType(TxnType.DEPOSIT)
                .status(TxnStatus.PENDING)
                .amount(amount)
                .currency("INR")
                .toAccountId(account.getId())
                .initiatedBy(user.getId())
                .channel(TxnChannel.PORTAL)
                .narration(request.getNarration())
                .requestedAt(Instant.now())
                .build());

        // post
        account.setAvailableBalance(account.getAvailableBalance().add(amount));
        accountRepository.save(account);

        ledgerEntryRepository.save(LedgerEntry.builder()
                .transactionId(txn.getId())
                .accountId(account.getId())
                .entryType(LedgerEntryType.CREDIT)
                .amount(amount)
                .runningBalance(account.getAvailableBalance())
                .postedAt(Instant.now())
                .build());

        txn.setStatus(TxnStatus.POSTED);
        txn.setPostedAt(Instant.now());
        transactionRepository.save(txn);

        log.info("Deposit posted: txnRef={}, accountId={}, amount={}", txn.getTxnRef(), account.getId(), amount);
        return toResponse(txn);
    }

    @Override
    @Transactional
    public TxnResponse withdraw(WithdrawRequest request) {
        Transaction existing = transactionRepository.findByTxnRef(request.getTxnRef()).orElse(null);
        if (existing != null) return toResponse(existing);

        User user = getCurrentUser();
        Account account = loadOwnedAccount(user, request.getAccountId());

        validateAccountActive(account);
        BigDecimal amount = validateAmount(request.getAmount());

        if (account.getAvailableBalance().compareTo(amount) < 0) {
            Transaction failed = transactionRepository.save(Transaction.builder()
                    .txnRef(request.getTxnRef())
                    .txnType(TxnType.WITHDRAWAL)
                    .status(TxnStatus.FAILED)
                    .amount(amount)
                    .currency("INR")
                    .fromAccountId(account.getId())
                    .initiatedBy(user.getId())
                    .channel(TxnChannel.PORTAL)
                    .narration(request.getNarration())
                    .failureReason("Insufficient funds")
                    .requestedAt(Instant.now())
                    .build());
            return toResponse(failed);
        }

        Transaction txn = transactionRepository.save(Transaction.builder()
                .txnRef(request.getTxnRef())
                .txnType(TxnType.WITHDRAWAL)
                .status(TxnStatus.PENDING)
                .amount(amount)
                .currency("INR")
                .fromAccountId(account.getId())
                .initiatedBy(user.getId())
                .channel(TxnChannel.PORTAL)
                .narration(request.getNarration())
                .requestedAt(Instant.now())
                .build());

        account.setAvailableBalance(account.getAvailableBalance().subtract(amount));
        accountRepository.save(account);

        ledgerEntryRepository.save(LedgerEntry.builder()
                .transactionId(txn.getId())
                .accountId(account.getId())
                .entryType(LedgerEntryType.DEBIT)
                .amount(amount)
                .runningBalance(account.getAvailableBalance())
                .postedAt(Instant.now())
                .build());

        txn.setStatus(TxnStatus.POSTED);
        txn.setPostedAt(Instant.now());
        transactionRepository.save(txn);

        log.info("Withdrawal posted: txnRef={}, accountId={}, amount={}", txn.getTxnRef(), account.getId(), amount);
        return toResponse(txn);
    }

    @Override
    @Transactional
    public TxnResponse transfer(TransferRequest request) {
        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new RuntimeException("fromAccountId and toAccountId cannot be same");
        }

        Transaction existing = transactionRepository.findByTxnRef(request.getTxnRef()).orElse(null);
        if (existing != null) return toResponse(existing);

        User user = getCurrentUser();
        Account from = loadOwnedAccount(user, request.getFromAccountId());
        Account to = accountRepository.findById(request.getToAccountId())
                .orElseThrow(() -> new RuntimeException("To account not found"));

        validateAccountActive(from);
        validateAccountActive(to);

        BigDecimal amount = validateAmount(request.getAmount());

        if (from.getAvailableBalance().compareTo(amount) < 0) {
            Transaction failed = transactionRepository.save(Transaction.builder()
                    .txnRef(request.getTxnRef())
                    .txnType(TxnType.TRANSFER)
                    .status(TxnStatus.FAILED)
                    .amount(amount)
                    .currency("INR")
                    .fromAccountId(from.getId())
                    .toAccountId(to.getId())
                    .initiatedBy(user.getId())
                    .channel(TxnChannel.PORTAL)
                    .narration(request.getNarration())
                    .failureReason("Insufficient funds")
                    .requestedAt(Instant.now())
                    .build());
            return toResponse(failed);
        }

        Transaction txn = transactionRepository.save(Transaction.builder()
                .txnRef(request.getTxnRef())
                .txnType(TxnType.TRANSFER)
                .status(TxnStatus.PENDING)
                .amount(amount)
                .currency("INR")
                .fromAccountId(from.getId())
                .toAccountId(to.getId())
                .initiatedBy(user.getId())
                .channel(TxnChannel.PORTAL)
                .narration(request.getNarration())
                .requestedAt(Instant.now())
                .build());

        // Important: update balances first, then ledger with running balance
        from.setAvailableBalance(from.getAvailableBalance().subtract(amount));
        to.setAvailableBalance(to.getAvailableBalance().add(amount));

        accountRepository.save(from);
        accountRepository.save(to);

        Instant postedAt = Instant.now();

        ledgerEntryRepository.save(LedgerEntry.builder()
                .transactionId(txn.getId())
                .accountId(from.getId())
                .entryType(LedgerEntryType.DEBIT)
                .amount(amount)
                .runningBalance(from.getAvailableBalance())
                .postedAt(postedAt)
                .build());

        ledgerEntryRepository.save(LedgerEntry.builder()
                .transactionId(txn.getId())
                .accountId(to.getId())
                .entryType(LedgerEntryType.CREDIT)
                .amount(amount)
                .runningBalance(to.getAvailableBalance())
                .postedAt(postedAt)
                .build());

        txn.setStatus(TxnStatus.POSTED);
        txn.setPostedAt(postedAt);
        transactionRepository.save(txn);

        log.info("Transfer posted: txnRef={}, from={}, to={}, amount={}",
                txn.getTxnRef(), from.getId(), to.getId(), amount);

        return toResponse(txn);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TxnResponse> accountTransactions(UUID accountId, LocalDate from, LocalDate to, String type) {
        User user = getCurrentUser();
        Account owned = loadOwnedAccount(user, accountId);

        Instant fromTs = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant toTs = to.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().minusMillis(1);

        List<Transaction> txns = transactionRepository
                .findByFromAccountIdOrToAccountIdAndRequestedAtBetweenOrderByRequestedAtDesc(
                        owned.getId(), owned.getId(), fromTs, toTs);

        if (type != null && !type.isBlank()) {
            TxnType t = TxnType.valueOf(type.trim().toUpperCase());
            txns = txns.stream().filter(x -> x.getTxnType() == t).toList();
        }

        return txns.stream().map(this::toResponse).toList();
    }

    // ---------------- helpers ----------------

    private BigDecimal validateAmount(BigDecimal amt) {
        if (amt == null || amt.signum() <= 0) throw new RuntimeException("amount must be > 0");
        return amt;
    }

    private void validateAccountActive(Account a) {
        if (a.getStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("Account is not ACTIVE");
        }
    }

    private Account loadOwnedAccount(User user, UUID accountId) {
        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Customer not found for user"));

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountId));

        if (!account.getCustomerId().equals(customer.getId())) {
            throw new RuntimeException("You cannot access other customer's account");
        }
        return account;
    }

    private User getCurrentUser() {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    private TxnResponse toResponse(Transaction t) {
        return TxnResponse.builder()
                .id(t.getId())
                .txnRef(t.getTxnRef())
                .txnType(t.getTxnType().name())
                .status(t.getStatus().name())
                .amount(t.getAmount())
                .fromAccountId(t.getFromAccountId())
                .toAccountId(t.getToAccountId())
                .requestedAt(t.getRequestedAt())
                .postedAt(t.getPostedAt())
                .failureReason(t.getFailureReason())
                .build();
    }
}