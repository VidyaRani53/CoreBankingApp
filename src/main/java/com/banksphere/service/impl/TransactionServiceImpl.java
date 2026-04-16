package com.banksphere.service.impl;

import com.banksphere.dto.txn.*;
import com.banksphere.entity.*;
import com.banksphere.entity.enums.*;
import com.banksphere.exception.InactiveAccountException;
import com.banksphere.repository.*;
import com.banksphere.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
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
        User user = getCurrentUser();

        // Staff usually performs deposits, but we allow self-deposits for testing
        Account account = accountRepository.findByAccountNo(request.getAccountNo())
                .orElseThrow(() -> new RuntimeException("Account not found: " + request.getAccountNo()));

        validateAccountActive(account);
        BigDecimal amount = validateAmount(request.getAmount());

        String txnRef = generateTxnRef(account.getAccountNo(), "DEP");

        Transaction txn = Transaction.builder()
                .txnRef(txnRef)
                .txnType(TxnType.DEPOSIT)
                .status(TxnStatus.PENDING)
                .amount(amount)
                .toAccountId(account.getId())
                .initiatedBy(user.getId())
                .narration(request.getNarration())
                .requestedAt(Instant.now())
                .build();

        account.setAvailableBalance(account.getAvailableBalance().add(amount));
        accountRepository.save(account);

        Transaction saved = transactionRepository.save(txn);
        createLedgerEntry(saved, account.getId(), LedgerEntryType.CREDIT, amount, account.getAvailableBalance());

        saved.setStatus(TxnStatus.POSTED);
        saved.setPostedAt(Instant.now());

        return toResponse(transactionRepository.save(saved));
    }

    @Override
    public TxnResponse withdraw(WithdrawRequest request) {
        return null;
    }

    @Override
    @Transactional
    public TxnResponse transfer(TransferRequest request) {
        User user = getCurrentUser();

        Account from = accountRepository.findByAccountNo(request.getFromAccountNo())
                .orElseThrow(() -> new RuntimeException("Source account not found"));
        Account to = accountRepository.findByAccountNo(request.getToAccountNo())
                .orElseThrow(() -> new RuntimeException("Destination account not found"));

        if (from.getAccountNo().equals(to.getAccountNo())) {
            throw new RuntimeException("Cannot transfer to the same account");
        }

        if ("CUSTOMER".equalsIgnoreCase(user.getUserType())) {
            validateOwnership(user, from);
        }

        validateAccountActive(from);
        validateAccountActive(to);

        BigDecimal amount = validateAmount(request.getAmount());

        String txnRef = generateTxnRef(from.getAccountNo(), "TRF");

        Transaction txn = Transaction.builder()
                .txnRef(txnRef)
                .txnType(TxnType.TRANSFER)
                .amount(amount)
                .fromAccountId(from.getId())
                .toAccountId(to.getId())
                .initiatedBy(user.getId())
                .narration(request.getNarration())
                .requestedAt(Instant.now())
                .build();

        // ✅ NEW: High-value rule
        if ("CUSTOMER".equalsIgnoreCase(user.getUserType())
                && amount.compareTo(new BigDecimal(100000)) > 0) {

            txn.setStatus(TxnStatus.PENDING_APPROVAL);
            return toResponse(transactionRepository.save(txn));
        }

        // Normal flow
        return postTransfer(txn, from, to);
    }

    private TxnResponse postTransfer(Transaction txn, Account from, Account to) {

        if (from.getAvailableBalance().compareTo(txn.getAmount()) < 0) {
            txn.setStatus(TxnStatus.FAILED);
            txn.setFailureReason("Insufficient funds");
            return toResponse(transactionRepository.save(txn));
        }

        from.setAvailableBalance(from.getAvailableBalance().subtract(txn.getAmount()));
        to.setAvailableBalance(to.getAvailableBalance().add(txn.getAmount()));

        accountRepository.save(from);
        accountRepository.save(to);

        txn.setStatus(TxnStatus.POSTED);
        txn.setPostedAt(Instant.now());

        Transaction saved = transactionRepository.save(txn);

        createLedgerEntry(saved, from.getId(), LedgerEntryType.DEBIT,
                txn.getAmount(), from.getAvailableBalance());

        createLedgerEntry(saved, to.getId(), LedgerEntryType.CREDIT,
                txn.getAmount(), to.getAvailableBalance());

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TxnResponse> getStatement(String accountNo, String interval, LocalDate from, LocalDate to, String type) {
        User user = getCurrentUser();
        Account account = accountRepository.findByAccountNo(accountNo)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Security check
        if ("CUSTOMER".equalsIgnoreCase(user.getUserType())) {
            validateOwnership(user, account);
        }

        // Calculate Date Range
        Instant startTs;
        Instant endTs = (to != null) ? to.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant() : Instant.now();

        if (interval != null && !interval.isBlank()) {
            startTs = switch (interval.toUpperCase()) {
                case "3MONTH" -> LocalDate.now().minusMonths(3).atStartOfDay(ZoneId.systemDefault()).toInstant();
                case "6MONTH" -> LocalDate.now().minusMonths(6).atStartOfDay(ZoneId.systemDefault()).toInstant();
                case "1YEAR" -> LocalDate.now().minusYears(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
                default -> from != null ? from.atStartOfDay(ZoneId.systemDefault()).toInstant() :
                        LocalDate.now().minusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            };
        } else {
            startTs = (from != null) ? from.atStartOfDay(ZoneId.systemDefault()).toInstant() :
                    LocalDate.now().minusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        }

        List<Transaction> txns = transactionRepository.findStatement(account.getId(), startTs, endTs);

        if (type != null && !type.isBlank()) {
            TxnType t = TxnType.valueOf(type.toUpperCase());
            return txns.stream().filter(x -> x.getTxnType() == t).map(this::toResponse).toList();
        }

        return txns.stream().map(this::toResponse).toList();
    }

    @Override
    public List<TxnResponse> accountTransactions(UUID accountId, LocalDate from, LocalDate to, String type) {
        return List.of();
    }

    // ---------------- Helpers ----------------

    private String generateTxnRef(String accNo, String prefix) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // Use last 4 digits of account for the ref
        String suffix = accNo.substring(accNo.length() - 4);
        return String.format("%s-%s-%s-%04d", prefix, date, suffix, (System.currentTimeMillis() % 10000));
    }

    private void createLedgerEntry(Transaction t, UUID accId, LedgerEntryType type, BigDecimal amt, BigDecimal bal) {
        ledgerEntryRepository.save(LedgerEntry.builder()
                .transactionId(t.getId())
                .accountId(accId)
                .entryType(type)
                .amount(amt)
                .runningBalance(bal)
                .postedAt(Instant.now())
                .build());
    }

    private void validateOwnership(User user, Account account) {
        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Customer profile not found"));
        if (!account.getCustomerId().equals(customer.getId())) {
            throw new RuntimeException("Access Denied: You do not own this account");
        }
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElseThrow();
    }

    private BigDecimal validateAmount(BigDecimal amt) {
        if (amt == null || amt.signum() <= 0) throw new RuntimeException("Amount must be greater than zero");
        return amt;
    }

    private void validateAccountActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new InactiveAccountException("Transactions allowed only for ACTIVE accounts");
        }
    }


    private TxnResponse toResponse(Transaction t) {
        // Find account numbers for the response mapping
        String fromNo = t.getFromAccountId() != null ?
                accountRepository.findById(t.getFromAccountId()).map(Account::getAccountNo).orElse("N/A") : null;
        String toNo = t.getToAccountId() != null ?
                accountRepository.findById(t.getToAccountId()).map(Account::getAccountNo).orElse("N/A") : null;

        return TxnResponse.builder()
                .id(t.getId())
                .txnRef(t.getTxnRef())
                .txnType(t.getTxnType().name())
                .status(t.getStatus().name())
                .amount(t.getAmount())
                .fromAccountNo(fromNo)
                .toAccountNo(toNo)
                .requestedAt(t.getRequestedAt())
                .postedAt(t.getPostedAt())
                .narration(t.getNarration())
                .failureReason(t.getFailureReason())
                .build();
    }
    @Override
    @Transactional(readOnly = true)
    public List<TxnResponse> getPendingHighValueTxns() {
        return transactionRepository
                .findByStatus(TxnStatus.PENDING_APPROVAL)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TxnResponse approveHighValueTxn(UUID txnId) {

        User csr = getCurrentUser();

        Transaction txn = transactionRepository.findById(txnId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (txn.getStatus() != TxnStatus.PENDING_APPROVAL) {
            throw new RuntimeException("Transaction not pending approval");
        }

        Account from = accountRepository.findById(txn.getFromAccountId()).orElseThrow();
        Account to = accountRepository.findById(txn.getToAccountId()).orElseThrow();

        TxnResponse response = postTransfer(txn, from, to);

        txn.setApprovedBy(csr.getId());
        txn.setApprovedAt(Instant.now());

        return response;
    }

    @Override
    @Transactional
    public TxnResponse rejectHighValueTxn(UUID txnId, String reason) {

        User csr = getCurrentUser();

        Transaction txn = transactionRepository.findById(txnId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (txn.getStatus() != TxnStatus.PENDING_APPROVAL) {
            throw new RuntimeException("Transaction not pending approval");
        }

        txn.setStatus(TxnStatus.REJECTED);
        txn.setFailureReason(reason);
        txn.setApprovedBy(csr.getId());
        txn.setApprovedAt(Instant.now());

        return toResponse(transactionRepository.save(txn));
    }


}