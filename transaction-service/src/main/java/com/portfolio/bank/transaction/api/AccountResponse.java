package com.portfolio.bank.transaction.api;

import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        Long userId,
        BigDecimal balance,
        String status) {}
