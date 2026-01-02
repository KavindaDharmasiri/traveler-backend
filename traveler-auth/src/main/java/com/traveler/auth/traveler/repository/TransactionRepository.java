package com.traveler.auth.traveler.repository;

import com.traveler.common.entity.Order;
import com.traveler.common.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionCode(String transactionCode);
}
