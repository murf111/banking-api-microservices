package com.portfolio.bank.account.internal;

import com.portfolio.bank.account.api.AccountResponse;
import com.portfolio.bank.account.api.AccountService;
import com.portfolio.bank.account.api.CreateAccountRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public AccountResponse createAccount(Long userId, CreateAccountRequest request) {

        AccountEntity account = new AccountEntity();
        account.setUserId(userId);
        account.setAccountNumber(generateSecureAccountNumber());
        account.setBalance(BigDecimal.ZERO);
        account.setCurrency(request.currency().toUpperCase());
        account.setStatus(Status.ACTIVE);

        AccountEntity savedAccount = accountRepository.save(account);
        return mapToResponse(savedAccount);
    }

    @Override
    public AccountResponse getAccount(Long accountId, Long userId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
                                .map(this::mapToResponse)
                                .orElseThrow(() -> new IllegalArgumentException("Account not found or access denied"));
    }

    @Override
    public List<AccountResponse> getUserAccounts(Long userId) {
        return accountRepository.findAllByUserId(userId).stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateBalance(Long accountId, BigDecimal amount) {
        AccountEntity account = accountRepository.findById(accountId)
                                                 .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            account.debit(amount.negate());
        } else {
            account.credit(amount);
        }

        accountRepository.save(account);
        // ^ Optimistic Locking triggers here automatically during the transaction flush
    }

    private Long generateSecureAccountNumber() {
        java.security.SecureRandom random = new java.security.SecureRandom();
        long accountNumber;
        do {
            // Generates a secure, positive 10-digit number
            accountNumber = 1000000000L + (Math.abs(random.nextLong()) % 9000000000L);
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }

    private AccountResponse mapToResponse(AccountEntity entity) {
        return new AccountResponse(
                entity.getId(),
                entity.getAccountNumber(),
                entity.getBalance(),
                entity.getCurrency(),
                entity.getStatus().name()
        );
    }
}