package com.portfolio.bank.transaction.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferRequest(
        @NotNull Long sourceAccountId,
        @NotNull Long destinationAccountId,
        @NotNull @DecimalMin(value = "0.01", message = "Transfer amount must be greater than zero")
        BigDecimal amount
) {}