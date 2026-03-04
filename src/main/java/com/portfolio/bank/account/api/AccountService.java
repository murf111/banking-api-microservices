package com.portfolio.bank.account.api;
import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
    AccountResponse createAccount(Long userId, CreateAccountRequest request);
    AccountResponse getAccount(Long accountId, Long userId);
    List<AccountResponse> getUserAccounts(Long userId);
    void updateBalance(Long accountId, BigDecimal amount);
}