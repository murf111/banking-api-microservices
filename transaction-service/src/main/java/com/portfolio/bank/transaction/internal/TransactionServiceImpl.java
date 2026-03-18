package com.portfolio.bank.transaction.internal;

import com.portfolio.bank.transaction.api.AccountResponse;
import com.portfolio.bank.transaction.api.AccountClient;
import com.portfolio.bank.transaction.api.MoneyTransferredEvent;
import com.portfolio.bank.transaction.api.TransactionResponse;
import com.portfolio.bank.transaction.api.TransactionService;
import com.portfolio.bank.transaction.api.TransferRequest;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountClient accountService; // Cross-module communication

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    @Transactional // Guarantees ACID properties for the entire transfer
    @CircuitBreaker(name = "accountService", fallbackMethod = "transferFallback")
    public TransactionResponse transfer(Long userId, TransferRequest request) {

        if (request.sourceAccountId().equals(request.destinationAccountId())) {
            throw new IllegalArgumentException("Cannot transfer funds to the same account");
        }

        // 1. Verify Ownership & Balance (getAccount throws if user doesn't own it)
        AccountResponse sourceAccount = accountService.getAccount(request.sourceAccountId());

        // SECURITY CHECK: Does the logged-in user actually own this account?
        if (!sourceAccount.userId().equals(userId)) {
            throw new AccessDeniedException("UNAUTHORIZED: You do not own this account!");
        }

        if (sourceAccount.balance().compareTo(request.amount()) < 0) {
            throw new IllegalArgumentException("Insufficient funds for transfer");
        }

        // 2. Execute Balance Modifications via the Account Module
        accountService.updateBalance(request.sourceAccountId(), request.amount().negate()); // Debit
        accountService.updateBalance(request.destinationAccountId(), request.amount());     // Credit

        // 3. Record the Immutable Ledger Entry
        TransactionEntity transaction = new TransactionEntity();
        transaction.setSourceAccountId(request.sourceAccountId());
        transaction.setDestinationAccountId(request.destinationAccountId());
        transaction.setAmount(request.amount());
        transaction.setStatus(Status.COMPLETED);
        transaction.setTimestamp(LocalDateTime.now());

        TransactionEntity savedTransaction = transactionRepository.save(transaction);

        MoneyTransferredEvent event = new MoneyTransferredEvent(
                savedTransaction.getSourceAccountId(),
                savedTransaction.getDestinationAccountId(),
                savedTransaction.getAmount(),
                savedTransaction.getTimestamp().toString()
        );

        kafkaTemplate.send("transaction-events", event);

        return new TransactionResponse(
                savedTransaction.getId(),
                savedTransaction.getSourceAccountId(),
                savedTransaction.getDestinationAccountId(),
                savedTransaction.getAmount(),
                savedTransaction.getStatus().name(),
                savedTransaction.getTimestamp()
        );
    }

    // The signature MUST match the original method exactly, with a Throwable added at the end.
    public TransactionResponse transferFallback(Long userId, TransferRequest request, Throwable throwable) {

        // 1. PASS-THROUGH LOCAL BUSINESS EXCEPTIONS
        // If the error was generated locally in this service, let it pass to the GlobalExceptionHandler
        if (throwable instanceof IllegalArgumentException || throwable instanceof AccessDeniedException) {
            throw (RuntimeException) throwable;
        }

        // 2. HANDLE REMOTE SERVICE EXCEPTIONS
        // If the error came from the Account Service via Feign
        if (throwable instanceof FeignException feignException) {

            // If Account Service returned 422, 400, or 500
            if (feignException.status() == 422 || feignException.status() == 400 || feignException.status() == 500) {
                // Rethrow the actual business exception so your GlobalExceptionHandler can catch it
                throw new RuntimeException("Business logic failed in Account Service: " + feignException.contentUTF8());
            }
        }

        // 3. TRUE INFRASTRUCTURE FAILURE
        // If the account-service is actually down, timeout, or connection refused
        throw new IllegalStateException("The Account Service is currently unavailable. Your transfer could not be processed. Please try again later.");
    }
}