package com.portfolio.bank.transaction.api;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        Long sourceAccountId,
        Long destinationAccountId,
        BigDecimal amount,
        String status,
        LocalDateTime timestamp
) {}