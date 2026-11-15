package com.banking.system.dto;

import com.banking.system.entity.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    private String accountNumber;
    private AccountType accountType;
    private BigDecimal balance;
    private String ownerName;
    private LocalDateTime createdAt;
}