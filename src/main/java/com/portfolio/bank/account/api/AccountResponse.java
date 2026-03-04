package com.portfolio.bank.account.api;
import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        Long accountNumber,
        BigDecimal balance,
        String currency,
        String status
) {}