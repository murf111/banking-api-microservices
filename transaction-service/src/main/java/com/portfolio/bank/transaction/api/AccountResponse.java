package com.portfolio.bank.transaction.api;

import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        BigDecimal balance,
        String status) {}
