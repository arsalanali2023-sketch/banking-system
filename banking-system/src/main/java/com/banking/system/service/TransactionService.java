package com.banking.system.service;

import com.banking.system.dto.TransactionRequest;
import com.banking.system.dto.TransactionResponse;
import com.banking.system.dto.TransferRequest;
import com.banking.system.entity.Account;
import com.banking.system.entity.Transaction;
import com.banking.system.entity.TransactionType;
import com.banking.system.entity.User;
import com.banking.system.exception.BankingException;
import com.banking.system.repository.AccountRepository;
import com.banking.system.repository.TransactionRepository;
import com.banking.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    private User getLoggedInUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BankingException("Authenticated user not found", HttpStatus.NOT_FOUND));
    }

    private String generateReference() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Transactional
    public TransactionResponse deposit(TransactionRequest request) {
        User user = getLoggedInUser();
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new BankingException("Account not found", HttpStatus.NOT_FOUND));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new BankingException("You are not authorized to deposit into this account", HttpStatus.FORBIDDEN);
        }

        account.setBalance(account.getBalance().add(request.getAmount()));
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .transactionReference(generateReference())
                .toAccountNumber(account.getAccountNumber())
                .amount(request.getAmount())
                .type(TransactionType.DEPOSIT)
                .status("SUCCESS")
                .description(request.getDescription() != null ? request.getDescription() : "Cash/Online Deposit")
                .build();

        return mapToResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionResponse withdraw(TransactionRequest request) {
        User user = getLoggedInUser();
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new BankingException("Account not found", HttpStatus.NOT_FOUND));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new BankingException("You are not authorized to withdraw from this account", HttpStatus.FORBIDDEN);
        }

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BankingException("Insufficient balance", HttpStatus.BAD_REQUEST);
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .transactionReference(generateReference())
                .fromAccountNumber(account.getAccountNumber())
                .amount(request.getAmount())
                .type(TransactionType.WITHDRAWAL)
                .status("SUCCESS")
                .description(request.getDescription() != null ? request.getDescription() : "Cash Withdrawal")
                .build();

        return mapToResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionResponse transfer(TransferRequest request) {
        if (request.getFromAccountNumber().equals(request.getToAccountNumber())) {
            throw new BankingException("Cannot transfer money to the same account", HttpStatus.BAD_REQUEST);
        }

        User user = getLoggedInUser();
        Account fromAccount = accountRepository.findByAccountNumber(request.getFromAccountNumber())
                .orElseThrow(() -> new BankingException("Source account not found", HttpStatus.NOT_FOUND));

        if (!fromAccount.getUser().getId().equals(user.getId())) {
            throw new BankingException("You are not authorized to transfer from this account", HttpStatus.FORBIDDEN);
        }

        Account toAccount = accountRepository.findByAccountNumber(request.getToAccountNumber())
                .orElseThrow(() -> new BankingException("Destination account not found", HttpStatus.NOT_FOUND));

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BankingException("Insufficient balance for transfer", HttpStatus.BAD_REQUEST);
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        Transaction transaction = Transaction.builder()
                .transactionReference(generateReference())
                .fromAccountNumber(fromAccount.getAccountNumber())
                .toAccountNumber(toAccount.getAccountNumber())
                .amount(request.getAmount())
                .type(TransactionType.TRANSFER)
                .status("SUCCESS")
                .description(request.getDescription() != null ? request.getDescription() : "Fund Transfer")
                .build();

        return mapToResponse(transactionRepository.save(transaction));
    }

    public List<TransactionResponse> getTransactionHistory(String accountNumber) {
        User user = getLoggedInUser();
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BankingException("Account not found", HttpStatus.NOT_FOUND));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new BankingException("You are not authorized to view transactions for this account", HttpStatus.FORBIDDEN);
        }

        return transactionRepository
                .findByFromAccountNumberOrToAccountNumberOrderByTimestampDesc(accountNumber, accountNumber)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .transactionReference(transaction.getTransactionReference())
                .fromAccountNumber(transaction.getFromAccountNumber())
                .toAccountNumber(transaction.getToAccountNumber())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .status(transaction.getStatus())
                .description(transaction.getDescription())
                .timestamp(transaction.getTimestamp())
                .build();
    }
}