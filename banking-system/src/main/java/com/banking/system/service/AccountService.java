package com.banking.system.service;

import com.banking.system.dto.AccountResponse;
import com.banking.system.dto.CreateAccountRequest;
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

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    private User getLoggedInUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BankingException("Authenticated user not found", HttpStatus.NOT_FOUND));
    }

    private String generateUniqueAccountNumber() {
        SecureRandom random = new SecureRandom();
        String accountNumber;
        do {
            long number = 100000000000L + (long) (random.nextDouble() * 900000000000L);
            accountNumber = String.valueOf(number);
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        User user = getLoggedInUser();
        BigDecimal initialBalance = request.getInitialDeposit() != null
                ? request.getInitialDeposit()
                : BigDecimal.ZERO;

        Account account = Account.builder()
                .accountNumber(generateUniqueAccountNumber())
                .accountType(request.getAccountType())
                .balance(initialBalance)
                .user(user)
                .build();

        Account savedAccount = accountRepository.save(account);

        if (initialBalance.compareTo(BigDecimal.ZERO) > 0) {
            Transaction transaction = Transaction.builder()
                    .transactionReference("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .toAccountNumber(savedAccount.getAccountNumber())
                    .amount(initialBalance)
                    .type(TransactionType.DEPOSIT)
                    .status("SUCCESS")
                    .description("Initial account opening deposit")
                    .build();
            transactionRepository.save(transaction);
        }

        return mapToResponse(savedAccount);
    }

    public List<AccountResponse> getMyAccounts() {
        User user = getLoggedInUser();
        return accountRepository.findByUser(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public AccountResponse getAccountDetails(String accountNumber) {
        User user = getLoggedInUser();
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BankingException("Account not found", HttpStatus.NOT_FOUND));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new BankingException("You are not authorized to view this account", HttpStatus.FORBIDDEN);
        }

        return mapToResponse(account);
    }

    private AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder()
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .ownerName(account.getUser().getFullName())
                .createdAt(account.getCreatedAt())
                .build();
    }
}