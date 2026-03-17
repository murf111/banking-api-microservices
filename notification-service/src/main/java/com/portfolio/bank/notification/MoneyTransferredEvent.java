package com.portfolio.bank.notification;

import java.math.BigDecimal;

// Local DTO to catch the Kafka JSON message
public record MoneyTransferredEvent(
        Long sourceAccountId,
        Long destinationAccountId,
        BigDecimal amount,
        String timestamp
) {}