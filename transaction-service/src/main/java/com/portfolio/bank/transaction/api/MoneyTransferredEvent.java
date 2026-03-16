package com.portfolio.bank.transaction.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MoneyTransferredEvent(
        Long sourceAccountId,
        Long destinationAccountId,
        BigDecimal amount,
        LocalDateTime occurredOn
) {}