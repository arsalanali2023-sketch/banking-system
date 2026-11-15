package com.banking.system.dto;

import com.banking.system.entity.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateAccountRequest {

    @NotNull(message = "Account type is required (SAVINGS or CURRENT)")
    private AccountType accountType;

    @DecimalMin(value = "0.0", message = "Initial deposit cannot be negative")
    private BigDecimal initialDeposit;
}