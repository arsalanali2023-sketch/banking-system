package com.banking.system.controller;

import com.banking.system.dto.AccountResponse;
import com.banking.system.dto.CreateAccountRequest;
import com.banking.system.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        return new ResponseEntity<>(accountService.createAccount(request), HttpStatus.CREATED);
    }

    @GetMapping
    public List<AccountResponse> getMyAccounts() {
        return accountService.getMyAccounts();
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccountDetails(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccountDetails(accountNumber));
    }
}