package com.portfolio.bank.account.api;
import jakarta.validation.constraints.NotBlank;

public record CreateAccountRequest(@NotBlank String currency) {}