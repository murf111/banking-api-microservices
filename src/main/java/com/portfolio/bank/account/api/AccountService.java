package com.portfolio.bank.account.api;
import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
    AccountResponse createAccount(String userEmail, CreateAccountRequest request);
    AccountResponse getAccount(Long accountId, String userEmail);
    List<AccountResponse> getUserAccounts(String userEmail);
    void updateBalance(Long accountId, BigDecimal amount);
}