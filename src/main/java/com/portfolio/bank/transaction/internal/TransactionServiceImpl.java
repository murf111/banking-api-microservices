package com.portfolio.bank.transaction.internal;

import com.portfolio.bank.account.api.AccountResponse;
import com.portfolio.bank.account.api.AccountService;
import com.portfolio.bank.transaction.api.MoneyTransferredEvent;
import com.portfolio.bank.transaction.api.TransactionResponse;
import com.portfolio.bank.transaction.api.TransactionService;
import com.portfolio.bank.transaction.api.TransferRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService; // Cross-module communication

    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional // Guarantees ACID properties for the entire transfer
    public TransactionResponse transfer(Long userId, TransferRequest request) {

        if (request.sourceAccountId().equals(request.destinationAccountId())) {
            throw new IllegalArgumentException("Cannot transfer funds to the same account");
        }

        // 1. Verify Ownership & Balance (getAccount throws if user doesn't own it)
        AccountResponse sourceAccount = accountService.getAccount(request.sourceAccountId(), userId);

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

        eventPublisher.publishEvent(new MoneyTransferredEvent(
                savedTransaction.getSourceAccountId(),
                savedTransaction.getDestinationAccountId(),
                savedTransaction.getAmount(),
                savedTransaction.getTimestamp()
        ));

        return new TransactionResponse(
                savedTransaction.getId(),
                savedTransaction.getSourceAccountId(),
                savedTransaction.getDestinationAccountId(),
                savedTransaction.getAmount(),
                savedTransaction.getStatus().name(),
                savedTransaction.getTimestamp()
        );
    }
}