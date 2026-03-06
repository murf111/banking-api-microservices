package com.portfolio.bank.transaction.internal;

import org.springframework.data.jpa.repository.JpaRepository;

interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
}