package com.portfolio.bank.transaction.api;

public interface TransactionService {
    TransactionResponse transfer(String userEmail, TransferRequest request);
}