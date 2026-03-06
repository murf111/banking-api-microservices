package com.portfolio.bank.transaction.api;

public interface TransactionService {
    TransactionResponse transfer(Long userId, TransferRequest request);
}