package com.portfolio.bank.transaction.api;

import com.portfolio.bank.shared.security.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "account-service", configuration = FeignConfig.class)
public interface AccountClient {

    // 1. Ask the Account Service for the Account details
    @GetMapping("/api/v1/accounts/{id}")
    AccountResponse getAccount(@PathVariable("id") Long id);

    // 2. Ask the Account Service to modify the balance
    @PutMapping("/api/v1/accounts/{id}/balance")
    void updateBalance(@PathVariable("id") Long id, @RequestParam("amount") BigDecimal amount);
}
