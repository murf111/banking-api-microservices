package com.portfolio.bank.account.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface AccountRepository extends JpaRepository<AccountEntity, Long> {
    List<AccountEntity> findAllByUserId(Long userId);
    Optional<AccountEntity> findByIdAndUserId(Long id, Long userId);
    boolean existsByAccountNumber(Long accountNumber);
}